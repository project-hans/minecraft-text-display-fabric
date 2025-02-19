package io.kouna.mcgui.browser

import org.bukkit.plugin.PluginLogger
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefPaintEvent
import org.cef.callback.*
import org.cef.handler.*
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import java.awt.Dimension
import java.awt.Point
import java.awt.Rectangle
import java.nio.ByteBuffer
import java.util.*
import java.util.function.Consumer
import java.util.logging.Level
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class MyCefClient(
    private var defaultSize: Dimension = Dimension(640, 360)
) :
    CefClientHandler(),
    CefRenderHandler, CefLoadHandler, CefDisplayHandler, CefContextMenuHandler, CefDialogHandler,
    CefDownloadHandler, CefDragHandler, CefFocusHandler, CefJSDialogHandler, CefKeyboardHandler,
    CefLifeSpanHandler, CefRequestHandler
{
    // =================================================================================================================
    // == GENERAL SHIT =================================================================================================
    // =================================================================================================================
    val logger = PluginLogger.getLogger("RenderTextPlugin/Browser")
    val storage = HashMap<Int, MyCefBrowserState>();

    fun stateOf(browser: CefBrowser?): MyCefBrowserState? {
        if (browser == null) {
            return null
        }

        val id = browser.identifier
        if (storage.containsKey(id)) {
            return storage[id]
        } else {
            val state = MyCefBrowserState(defaultSize.clone() as Dimension)
            storage[id] = state
            return state
        }
    }

    fun <T> withState(browser: CefBrowser?, handler: (MyCefBrowserState) -> T): T? {
        val state = this.stateOf(browser)
        if (state != null) {
            return handler(state)
        }
        return null
    }

    private fun bLogPrefix(browser: CefBrowser?, state: MyCefBrowserState? = null): String {
        return "jCEF[browserId:${browser?.identifier}]: "
    }

    fun bLog(browser: CefBrowser?, onState: (MyCefBrowserState) -> String, level: Level = Level.INFO) {
        val state = stateOf(browser) ?: return;
        bLog(browser = browser, message = onState(state), level = level)
    }

    fun bLog(browser: CefBrowser?, message: String, level: Level = Level.INFO) {
        logger.log(level, "${bLogPrefix(browser)}${message}")
    }

    // =================================================================================================================
    // == IMPLEMENT CefClientHandler ===================================================================================
    // =================================================================================================================
    override fun getBrowser(browserId: Int): CefBrowser {
        return storage[browserId]?.browser!!
    }

    override fun getAllBrowser(): Array<Any> {
        return storage.map { entry -> entry.value.browser!! }.toTypedArray()
    }

    override fun getContextMenuHandler(): CefContextMenuHandler {
        return this
    }

    override fun getDialogHandler(): CefDialogHandler {
        return this
    }

    override fun getDisplayHandler(): CefDisplayHandler {
        return this
    }

    override fun getDownloadHandler(): CefDownloadHandler {
        return this
    }

    override fun getDragHandler(): CefDragHandler {
        return this
    }

    override fun getFocusHandler(): CefFocusHandler {
        return this
    }

    override fun getJSDialogHandler(): CefJSDialogHandler {
        return this
    }

    override fun getKeyboardHandler(): CefKeyboardHandler {
        return this
    }

    override fun getLifeSpanHandler(): CefLifeSpanHandler {
        return this
    }

    override fun getLoadHandler(): CefLoadHandler {
        return this
    }

    override fun getPrintHandler(): CefPrintHandler? {
        TODO("Not yet implemented")
    }

    override fun getRenderHandler(): CefRenderHandler {
        return this
    }

    override fun getRequestHandler(): CefRequestHandler {
        return this
    }

    override fun getWindowHandler(): CefWindowHandler? {
        TODO("Not yet implemented")
    }

    // =================================================================================================================
    // == IMPLEMENT CefRenderHandler ===================================================================================
    // =================================================================================================================
    private val onPaintListeners = HashSet<Consumer<CefPaintEvent>>()

    override fun getViewRect(browser: CefBrowser?): Rectangle {
        bLog(browser, "stub:getViewRect")
        val state = stateOf(browser) ?: return Rectangle(Point(0, 0), defaultSize)
        return Rectangle(state.scroll, state.size)
    }

    override fun getScreenInfo(browser: CefBrowser?, screenInfo: CefScreenInfo?): Boolean {
        bLog(browser, "stub:getScreenInfo screenInfo:$screenInfo");
        return true;
    }

    override fun getScreenPoint(browser: CefBrowser?, point: Point?): Point {
        return point ?: Point(0, 0)
    }

    override fun onPopupShow(browser: CefBrowser?, show: Boolean) {
        bLog(browser, "stub:onPopupShow show:$show")
    }

    override fun onPopupSize(browser: CefBrowser?, rectangle: Rectangle?) {
        bLog(browser, "stub:onPopupSize rectangle:$rectangle")
    }

    override fun onPaint(browser: CefBrowser?, popup: Boolean, dirtyRects: Array<out Rectangle>?, renderedFrame: ByteBuffer?, width: Int, height: Int) {
        bLog(browser, "stub:onPaint popup:$popup dirtyRectangles:$dirtyRects renderedFrame:$renderedFrame width:$width height:$height")
        val state = stateOf(browser)
        val event = CefPaintEvent(browser, popup, dirtyRects, renderedFrame, width, height)
        for (listener in onPaintListeners) {
            try {
                listener.accept(event)
            } catch (e: Exception) {
                bLog(browser, e.stackTraceToString(), Level.WARNING)
            }
        }
        return dirtyRects?.let { state?.doPaint(it, renderedFrame, width, height) } ?: Unit
    }

    override fun addOnPaintListener(paintEventConsumer: Consumer<CefPaintEvent>?) {
        if (paintEventConsumer != null) {
            onPaintListeners.add(paintEventConsumer)
        }
    }

    override fun setOnPaintListener(paintEventConsumer: Consumer<CefPaintEvent>?) {
        onPaintListeners.clear()
        addOnPaintListener(paintEventConsumer)
    }

    override fun removeOnPaintListener(paintEventConsumer: Consumer<CefPaintEvent>?) {
        if (paintEventConsumer != null) {
            onPaintListeners.remove(paintEventConsumer)
        }
    }

    override fun onCursorChange(browser: CefBrowser?, cursor: Int): Boolean {
        val state = stateOf(browser) ?: return false;
        state.cursorType = cursor;
        return true;
    }

    override fun startDragging(browser: CefBrowser?, dragData: CefDragData?, p2: Int, p3: Int, p4: Int): Boolean {
        bLog(browser , "stub:startDragging dragData:$dragData p2:$p2 p3:$p3 p4:$p4")
        return false
    }

    override fun updateDragCursor(browser: CefBrowser?, cursor: Int) {
        bLog(browser, "stub:updateDragCursor cursor:$cursor")
        onCursorChange(browser, cursor)
    }

    // =================================================================================================================
    // == IMPLEMENT CefLoadHandler =====================================================================================
    // =================================================================================================================
    override fun onLoadingStateChange(browser: CefBrowser?, p1: Boolean, p2: Boolean, p3: Boolean) {
        bLog(browser, "stub:onLoadingStateChange p1:$p1 p2:$p2 p3:$p3")
    }

    override fun onLoadStart(browser: CefBrowser?, frame: CefFrame?, transitionType: CefRequest.TransitionType?) {
        bLog(browser, "stub:onLoadStart ${frameString(frame)} transitionType:$transitionType")
    }

    override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatus: Int) {
        bLog(browser, "stub:onLoadEnd ${frameString(frame)} status:$httpStatus")
    }

    override fun onLoadError(browser: CefBrowser?, frame: CefFrame?, errorCode: CefLoadHandler.ErrorCode?, message: String?, source: String?) {
        bLog(browser, "stub:onLoadError ${frameString(frame)} error_code:$errorCode message:$message source:$source")
    }

    // =================================================================================================================
    // == IMPLEMENT CefDisplayHandler ==================================================================================
    // =================================================================================================================
    override fun onAddressChange(browser: CefBrowser?, frame: CefFrame?, url: String?) {
        bLog(browser, "stub:onAddressChange ${frameString(frame)} newUrl:$url")
    }

    override fun onTitleChange(browser: CefBrowser?, title: String?) {
        bLog(browser, "stub:onTitleChange title:$title")
    }

    override fun onFullscreenModeChange(browser: CefBrowser?, isFullScreen: Boolean) {
        bLog(browser, "stub:onFullscreenModeChange isFulScreen:$isFullScreen")
    }

    override fun onTooltip(browser: CefBrowser?, tooltipText: String?): Boolean {
        bLog(browser, "stub:onTooltip tooltipText:$tooltipText")
        return false
    }

    override fun onStatusMessage(browser: CefBrowser?, statusMessage: String?) {
        bLog(browser, "stub:onStatusMessage statusMessage:$statusMessage")
    }

    override fun onConsoleMessage(
        browser: CefBrowser?,
        logLevel: CefSettings.LogSeverity?,
        message: String?,
        source: String?,
        line: Int
    ): Boolean {
        bLog(browser, "stub:consoleMessage [$logLevel] $message ($source:$line}")
        return true
    }

    // =================================================================================================================
    // == IMPLEMENT CefContextMenuHandler ==============================================================================
    // =================================================================================================================
    override fun onBeforeContextMenu(browser: CefBrowser?, frame: CefFrame?, params: CefContextMenuParams?, model: CefMenuModel?) {
        bLog(browser, "stub:onBeforeContextMenu ${frameString(frame)} params:$params model:$model")
    }

    override fun onContextMenuCommand(
        browser: CefBrowser?,
        frame: CefFrame?,
        params: CefContextMenuParams?,
        commandId: Int,
        flags: Int
    ): Boolean {
        bLog(browser, "stub:onContextMenuCommand ${frameString(frame)} params:$params commandId:$commandId flags:$flags")
        return false
    }

    override fun onContextMenuDismissed(browser: CefBrowser?, frame: CefFrame?) {
        bLog(browser, "stub:onContextMenuDismissed ${frameString(frame)}")
    }

    // =================================================================================================================
    // == IMPLEMENT CefDialogHandler ===================================================================================
    // =================================================================================================================
    override fun onFileDialog(
        browser: CefBrowser?,
        dialogMode: CefDialogHandler.FileDialogMode?,
        p2: String?,
        p3: String?,
        p4: Vector<String>?,
        p5: Vector<String>?,
        p6: Vector<String>?,
        p7: CefFileDialogCallback?
    ): Boolean {
        bLog(browser, "stub:onFileDialog dialogMode:$dialogMode p2:$p2 p3:$p3 p4:$p4 p5:$p5 p6:$p6 p7:$p7")
        return false;
    }

    override fun onBeforeDownload(
        browser: CefBrowser?,
        item: CefDownloadItem?,
        name: String?,
        callback: CefBeforeDownloadCallback?
    ): Boolean {
        bLog(browser, "stub:onBeforeDownload item:$item name:$name callback:$callback")
        return false
    }

    override fun onDownloadUpdated(browser: CefBrowser?, item: CefDownloadItem?, callback: CefDownloadItemCallback?) {
        bLog(browser, "stub:onBeforeDownload item:$item callback:$callback")
    }

    // =================================================================================================================
    // == IMPLEMENT CefDragHandler =====================================================================================
    // =================================================================================================================
    override fun onDragEnter(browser: CefBrowser?, data: CefDragData?, mask: Int): Boolean {
        bLog(browser, "stub:onDragEnter data:$data mask:$mask")
        return false
    }

    // =================================================================================================================
    // == IMPLEMENT CefDragHandler =====================================================================================
    // =================================================================================================================
    override fun onTakeFocus(browser: CefBrowser?, isFocused: Boolean) {
        bLog(browser, "stub:onTakeFocus isFocused:$isFocused")
    }

    override fun onSetFocus(browser: CefBrowser?, source: CefFocusHandler.FocusSource?): Boolean {
        bLog(browser, "stub:onSetFocus source:$source")
        return false
    }

    override fun onGotFocus(browser: CefBrowser?) {
        bLog(browser, "stub:onGotFocus")
    }

    // =================================================================================================================
    // == IMPLEMENT CefJSDialogHandler =================================================================================
    // =================================================================================================================
    override fun onJSDialog(
        browser: CefBrowser?,
        origin: String?,
        type: CefJSDialogHandler.JSDialogType?,
        message: String?,
        defaultPrompt: String?,
        callback: CefJSDialogCallback?,
        override: BoolRef?
    ): Boolean {
        bLog(browser, "stub:onJSDialog \n" +
                "\torigin:$origin\n" +
                "\ttype:$type\n" +
                "\tmessage:$message\n" +
                "\tdefaultPrompt:$defaultPrompt\n" +
                "\tcallback:$callback\n" +
                "\toverride:$override")
        return false
    }

    override fun onBeforeUnloadDialog(browser: CefBrowser?, message: String?, isReload: Boolean, callback: CefJSDialogCallback?): Boolean {
        bLog(browser, "stub:onBeforeUnloadDialog message:$message isReload:$isReload callback:$callback")
        return false
    }

    override fun onResetDialogState(browser: CefBrowser?) {
        bLog(browser, "stub:onResetDialogState")
    }

    override fun onDialogClosed(browser: CefBrowser?) {
        bLog(browser, "stub:onDialogClosed")
    }

    // =================================================================================================================
    // == IMPLEMENT CefKeyboardHandler =================================================================================
    // =================================================================================================================
    override fun onPreKeyEvent(browser: CefBrowser?, event: CefKeyboardHandler.CefKeyEvent?, isKeyboardShortcut: BoolRef?): Boolean {
        bLog(browser, "stub:onPreKeyEvent event:$event isKeyboardShortcut:$isKeyboardShortcut")
        return false
    }

    override fun onKeyEvent(browser: CefBrowser?, event: CefKeyboardHandler.CefKeyEvent?): Boolean {
        bLog(browser, "stub:onKeyEvent event:$event")
        return false
    }

    // =================================================================================================================
    // == IMPLEMENT CefLifespanHandler =================================================================================
    // =================================================================================================================
    override fun onBeforePopup(browser: CefBrowser?, frame: CefFrame?, targetUrl: String?, targetName: String?): Boolean {
        bLog(browser, "stub:onBeforePopup ${frameString(frame)} targetUrl:$targetUrl targetName:$targetName")
        return false
    }

    override fun onAfterCreated(browser: CefBrowser?) {
        bLog(browser, "stub:onAfterCreated")
    }

    override fun onAfterParentChanged(browser: CefBrowser?) {
        bLog(browser, "stub:onAfterParentChanged")
    }

    override fun doClose(browser: CefBrowser?): Boolean {
        bLog(browser, "stub:doClose")
        return true
    }

    override fun onBeforeClose(browser: CefBrowser?) {
        bLog(browser, "stub:onBeforeClose")
    }

    // =================================================================================================================
    // == IMPLEMENT CefRequestHandler ==================================================================================
    // =================================================================================================================
    override fun onBeforeBrowse(browser: CefBrowser?, frame: CefFrame?, request: CefRequest?, navType: Boolean, isRedirect: Boolean): Boolean {
        bLog(browser, "stub:onBeforeBrowse ${frameString(frame)} request:$request navType:$navType isRedirect:$isRedirect")
        return withState(browser) { it.cefRequestHandler?.onBeforeBrowse(browser, frame, request, navType, isRedirect) }
            ?: true;
    }

    override fun onOpenURLFromTab(browser: CefBrowser?, frame: CefFrame?, targetUrl: String?, isUserGesture: Boolean): Boolean {
        bLog(browser, "stub:onOpenURLFromTab ${frameString(frame)} targetUrl:$targetUrl isUserGesture:$isUserGesture")
        return withState(browser) { it.cefRequestHandler?.onOpenURLFromTab(browser, frame, targetUrl, isUserGesture) }
            ?: true;
    }

    override fun getResourceRequestHandler(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?,
        isNavigation: Boolean,
        isDownload: Boolean,
        requestInitiator: String?,
        disableDefaultHanding: BoolRef?
    ): CefResourceRequestHandler? {
        bLog(browser, "stub:getResourceRequestHandler\n" +
                "\t${frameString(frame)}\n" +
                "\trequest:$request\n" +
                "\tisNavigation:$isNavigation\n" +
                "\tisDownload:$isDownload\n" +
                "\trequestInitiator:$requestInitiator\n" +
                "\tdisableDefaultHandling:$disableDefaultHanding")
        return withState(browser) { it.cefRequestHandler?.getResourceRequestHandler(browser, frame, request, isNavigation, isDownload, requestInitiator, disableDefaultHanding)}
    }

    override fun getAuthCredentials(
        browser: CefBrowser?,
        originUrl: String?,
        isProxy: Boolean,
        host: String?,
        port: Int,
        realm: String?,
        scheme: String?,
        callback: CefAuthCallback?
    ): Boolean {
        bLog(browser, "stub:getAuthCredentials\n" +
                "\toriginUrl:$originUrl\n" +
                "\tisProxy:$isProxy\n" +
                "\thost:$host\n" +
                "\tport:$port\n" +
                "\trealm:$realm\n" +
                "\tscheme:$scheme\n" +
                "\tcallback:$callback")
        return withState(browser) { it.cefRequestHandler?.getAuthCredentials(browser, originUrl, isProxy, host, port, realm, scheme, callback)} ?: true
    }

    override fun onCertificateError(
        browser: CefBrowser?,
        errorCode: CefLoadHandler.ErrorCode?,
        requestUrl: String?,
        callback: CefCallback?
    ): Boolean {
        bLog(browser, "stub:onCertificateError errorCode:$errorCode requestUrl:$requestUrl callback:$callback")
        return withState(browser) { it.cefRequestHandler?.onCertificateError(browser, errorCode, requestUrl, callback)} ?: true
    }

    override fun onRenderProcessTerminated(
        browser: CefBrowser?,
        status: CefRequestHandler.TerminationStatus?,
        p2: Int,
        p3: String?
    ) {
        bLog(browser, "stub:onRenderProcessTerminated status:$status p2:$p2 p3:$p3")
        return withState(browser) { it.cefRequestHandler?.onRenderProcessTerminated(browser, status, p2, p3)} ?: Unit
    }
}

fun frameString (frame: CefFrame?, param: String = "frame"): String {
    if (frame == null) {
        return "$param:null"
    }

    return "$param:{name:${frame.name} id:${frame.identifier} url:${frame.url}}"
}
