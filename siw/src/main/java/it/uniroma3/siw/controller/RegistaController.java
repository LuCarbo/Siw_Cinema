package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Regista;
import it.uniroma3.siw.service.RegistaService;
import it.uniroma3.siw.validator.RegistaValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistaController {

    @Autowired
    private RegistaService registaService;

    @Autowired
    private RegistaValidator registaValidator;

    @GetMapping("/regista/{id}")
    public String getRegista(@PathVariable("id") Long id, Model model) {
        Regista regista = registaService.getRegista(id);
        if (regista == null) {
            return "redirect:/";
        }
        model.addAttribute("regista", regista);
        return "regista/detail";
    }

    @GetMapping("/registi/nuovo")
    public String showCreateRegistaForm(Model model) {
        model.addAttribute("regista", new Regista());
        return "regista/form";
    }

    @PostMapping("/registi/salva")
    public String saveRegista(@Valid @ModelAttribute("regista") Regista regista,
                             BindingResult bindingResult,
                             Model model) {
        registaValidator.validate(regista, bindingResult);

        if (bindingResult.hasErrors()) {
            return "regista/form";
        }

        registaService.saveRegista(regista);
        return "redirect:/regista/" + regista.getId();
    }

    @GetMapping("/registi/modifica/{id}")
    public String showEditRegistaForm(@PathVariable("id") Long id, Model model) {
        Regista regista = registaService.getRegista(id);
        if (regista == null) {
            return "redirect:/";
        }
        model.addAttribute("regista", regista);
        return "regista/form";
    }

    @PostMapping("/registi/{id}/elimina")
    public String deleteRegista(@PathVariable("id") Long id) {
        registaService.deleteRegista(id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/registi/elimina/{id}")
    public String deleteRegistaGet(@PathVariable("id") Long id) {
        registaService.deleteRegista(id);
        return "redirect:/admin/dashboard";
    }
}
