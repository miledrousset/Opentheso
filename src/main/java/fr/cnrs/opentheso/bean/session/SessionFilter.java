package fr.cnrs.opentheso.bean.session;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.IOException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionFilter implements Filter {

    @Inject private SessionControl sessionControl;
    @Inject private ConceptView conceptView;
     @Inject private SelectedTheso selectedTheso;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
       
        if(req.getSession().isNew()){
        //    chain.doFilter(request, response);
   //         sessionControl.redirectToIndex();
    /*        if(FacesContext.getCurrentInstance()!=null)
                FacesContext.getCurrentInstance().getExternalContext().redirect((((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRequestURI()) + "/index.xhtml");
            else
                chain.doFilter(request, response);*/
        } else {
            try {
                selectedTheso.getSelectedIdTheso();
            } catch (Error | ViewExpiredException se) {
            }             
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        
    }
    
}
