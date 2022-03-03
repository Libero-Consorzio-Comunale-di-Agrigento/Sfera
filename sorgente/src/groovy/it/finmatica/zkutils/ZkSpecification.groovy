package it.finmatica.zkutils

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockServletContext
import org.zkoss.zk.ui.Desktop
import org.zkoss.zk.ui.Execution
import org.zkoss.zk.ui.Page
import org.zkoss.zk.ui.WebApp
import org.zkoss.zk.ui.http.ExecutionImpl
import org.zkoss.zk.ui.http.SimpleWebApp
import org.zkoss.zk.ui.impl.DesktopImpl
import org.zkoss.zk.ui.sys.ExecutionsCtrl
import org.zkoss.zk.ui.util.Configuration
import spock.lang.Specification

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by esasdelli on 14/09/2017.
 */
class ZkSpecification extends Specification {

    def setup () {
        ServletContext servletContext = new MockServletContext()
        HttpServletRequest request = new MockHttpServletRequest()
        HttpServletResponse response = new MockHttpServletResponse()
        WebApp webApp = new SimpleWebApp()
        webApp.init(servletContext, Mock(Configuration))
        Desktop desktop = new DesktopImpl(webApp, "", "", "", request)
        Page page = Mock(Page)
        Execution execution = new ExecutionImpl(servletContext, request, response, desktop, page)
        ExecutionsCtrl.setCurrent(execution)
    }

    def cleanup () {
        ExecutionsCtrl.setCurrent(null)
    }
}
