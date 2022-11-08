package fr.cnrs.opentheso.bean.session;

/**
 *
 * @author miled.rousset
 */
import java.util.Iterator;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

public class ViewExpiredExceptionHandler extends ExceptionHandlerWrapper {
    private ExceptionHandler handler;

    public ViewExpiredExceptionHandler(ExceptionHandler handler) {
        this.handler = handler;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return handler;
    }

    @Override
    public void handle() throws FacesException {
        //iterate over unhandler exceptions using the iterator returned from getUnhandledExceptionQueuedEvents().iterator()
        for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents()
                .iterator(); i.hasNext();) {
            ExceptionQueuedEvent queuedEvent = i.next();
            ExceptionQueuedEventContext queuedEventContext = (ExceptionQueuedEventContext) queuedEvent
                    .getSource();
            Throwable throwable = queuedEventContext.getException();
            if (throwable instanceof ViewExpiredException) {
                ViewExpiredException viewExpiredException = (ViewExpiredException) throwable;
                FacesContext facesContext = FacesContext.getCurrentInstance();
                Map<String, Object> map = facesContext.getExternalContext()
                        .getRequestMap();
                NavigationHandler navigationHandler = facesContext
                        .getApplication().getNavigationHandler();
                try {
                  //  System.gc();
                    map.put("currentViewId", viewExpiredException.getViewId());
                    navigationHandler
                            .handleNavigation(facesContext, null,
                                    "/errorPages/timeOut.xhtml?faces-redirect=true");//"viewExpired");
                    facesContext.renderResponse();
                    System.gc();
      //              System.gc();
               //     System.runFinalization ();
                } finally {
                    i.remove();
                }
            }
        }
        getWrapped().handle();
    }

}
