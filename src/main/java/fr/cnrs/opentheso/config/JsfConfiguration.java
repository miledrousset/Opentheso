package fr.cnrs.opentheso.config;

import com.sun.faces.config.ConfigureListener;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.ServletContext;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ServletContextAware;


@Configuration
public class JsfConfiguration  implements ServletContextAware {


    @Override
    public void setServletContext(ServletContext servletContext) {
        servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", Boolean.TRUE.toString());
        servletContext.setInitParameter("javax.faces.FACELETS_SKIP_COMMENTS", Boolean.TRUE.toString());

        servletContext.setInitParameter("facelets.DEVELOPMENT", Boolean.TRUE.toString());

        servletContext.setInitParameter("javax.faces.DEFAULT_SUFFIX", ".xhtml");
        servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Development");
        servletContext.setInitParameter("javax.faces.FACELETS_REFRESH_PERIOD", "1");
        servletContext.setInitParameter("javax.faces.FACELETS_LIBRARIES", "/WEB-INF/springsecurity.taglib.xml");

        servletContext.setInitParameter("primefaces.CLIENT_SIDE_VALIDATION", Boolean.TRUE.toString());
        servletContext.setInitParameter("primefaces.THEME", "nova-light");
        servletContext.setInitParameter("primefaces.UPLOADER", "commons");
        servletContext.setInitParameter("primefaces.MOVE_SCRIPTS_TO_BOTTOM", Boolean.TRUE.toString());
    }

    @Bean
    public ServletRegistrationBean<jakarta.faces.webapp.FacesServlet> facesServletRegistration() {
        ServletRegistrationBean<jakarta.faces.webapp.FacesServlet> registrationBean = new ServletRegistrationBean<>(new FacesServlet(), "*.xhtml");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }

    @Bean
    public ServletListenerRegistrationBean<ConfigureListener> jsfConfigureListener() {
        return new ServletListenerRegistrationBean<>(new ConfigureListener());
    }
}
