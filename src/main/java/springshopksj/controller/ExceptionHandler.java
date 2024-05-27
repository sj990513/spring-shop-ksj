package springshopksj.controller;


import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/join";
    }

}
