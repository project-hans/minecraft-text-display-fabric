package ar.caes.textdisplaygui.gui

import java.awt.Color
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JEditorPane
import javax.swing.SwingUtilities
import javax.swing.text.View
import javax.swing.text.html.BasicHTML
import javax.swing.text.html.HTML


/**
 * Holds the result of rendering HTML:
 * - [image] contains the rendered BMP.
 * - [interactiveAreas] maps bounding boxes (clickable areas) to their associated interactive tag (the element id).
 */
data class RenderResult(
    val image: BufferedImage,
    val interactiveAreas: Map<Rectangle, String>
)

object Html2BmpRenderer {

    /**
     * Executes [block] on the Swing Event Dispatch Thread.
     */
    private fun runOnEDT(block: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) {
            block()
        } else {
            SwingUtilities.invokeAndWait { block() }
        }
    }

    /**
     * Renders the given HTML string into a [RenderResult] containing:
     *  - a [BufferedImage] with the rendered content at the given resolution, and
     *  - a map of interactive areas (bounding boxes and associated element IDs).
     *
     * An interactive area is detected if the element’s class attribute is "interactive".
     *
     * @param html the HTML content to render.
     * @param width the width of the resulting image.
     * @param height the height of the resulting image.
     * @return a [RenderResult] containing the rendered image and interactive areas.
     */
    fun render(html: String, width: Int, height: Int): RenderResult {
        var result: RenderResult? = null

        runOnEDT {
            // Create and configure the editor pane.
            val editorPane = JEditorPane("text/html", html).apply {
                size = Dimension(width, height)
                isEditable = false
            }
            editorPane.validate()

            // Create an image with a white background.
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            val graphics = image.createGraphics()
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, width, height)

            // Render the HTML content into the image.
            editorPane.print(graphics)
            graphics.dispose()

            // Extract interactive areas from the HTML view hierarchy.
            val interactiveAreas = mutableMapOf<Rectangle, String>()
            val rootView: View = BasicHTML.getRootView(editorPane)
            extractInteractiveAreas(rootView, editorPane, interactiveAreas)

            result = RenderResult(image, interactiveAreas)
        }

        return result!!
    }

    /**
     * Renders the given HTML string and writes the BMP image to [outputFile].
     *
     * @param html the HTML content to render.
     * @param width the width of the resulting image.
     * @param height the height of the resulting image.
     * @param outputFile the file to which the BMP image will be written.
     * @return a [RenderResult] containing the rendered image and interactive areas.
     */
    fun renderToFile(html: String, width: Int, height: Int, outputFile: File): RenderResult {
        val result = render(html, width, height)
        ImageIO.write(result.image, "bmp", outputFile)
        return result
    }

    /**
     * Recursively traverses the view hierarchy to locate interactive areas.
     *
     * An interactive area is detected by the element’s class attribute being "interactive".
     * If such an element is found and it has an ID, a bounding box is computed.
     *
     * @param view the current view in the hierarchy.
     * @param editorPane the [JEditorPane] used for rendering.
     * @param interactiveAreas the map collecting bounding boxes and associated element IDs.
     */
    private fun extractInteractiveAreas(
        view: View,
        editorPane: JEditorPane,
        interactiveAreas: MutableMap<Rectangle, String>
    ) {
        val element = view.element
        val attrs = element.attributes

        // Check if this element has a class attribute equal to "interactive"
        val clazz = attrs.getAttribute(HTML.Attribute.CLASS) as? String
        if (clazz == "interactive") {
            // Instead of an href, we now look for the element's id.
            val id = attrs.getAttribute(HTML.Attribute.ID) as? String
            if (id != null) {
                try {
                    val startOffset = element.startOffset
                    val endOffset = element.endOffset
                    val startRect = editorPane.modelToView(startOffset)
                    val endRect = editorPane.modelToView(endOffset - 1)
                    if (startRect != null && endRect != null) {
                        // Compute a bounding rectangle that spans from the start to the end of the element.
                        val x = startRect.x
                        val y = startRect.y
                        val width = (endRect.x + endRect.width) - startRect.x
                        val height = maxOf(startRect.height, endRect.height)
                        interactiveAreas[Rectangle(x, y, width, height)] = id
                    }
                } catch (e: Exception) {
                    // Skip this element in case of an exception (e.g. modelToView failing).
                }
            }
        }
        // Recursively process child views.
        for (i in 0 until view.childCount) {
            val child = view.getView(i)
            extractInteractiveAreas(child, editorPane, interactiveAreas)
        }
    }
}
