package fr.cnrs.opentheso.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;


@ControllerAdvice
public class GlobalExceptionHandler {

    // Gestion des erreurs 500
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(HttpServletRequest request, Exception ex, Model model) {

        System.out.println("ERRuuuur : " + ex.getMessage());
        if (getHttpStatus(request).equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            model.addAttribute("errorMessage", ex.getMessage());
            return new ModelAndView("redirect:/errorPages/error500.xhtml");
        }

        model.addAttribute("errorMessage", ex.getMessage());
        return new ModelAndView("redirect:/errorPages/errorGeneric.xhtml");
    }

    // Gestion des erreurs 404
    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNotFoundException(NoHandlerFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return new ModelAndView("redirect:/errorPages/error404.xhtml");
    }

    private HttpStatus getHttpStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }
}
