package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Sala;
import it.uniroma3.siw.service.ProiezioneService;
import it.uniroma3.siw.service.SalaService;
import it.uniroma3.siw.validator.SalaValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class SalaController {

    @Autowired
    private SalaService salaService;

    @Autowired
    private ProiezioneService proiezioneService;

    @Autowired
    private SalaValidator salaValidator;

    @GetMapping("/sala/{id}")
    public String getSala(@PathVariable("id") Long id, Model model) {
        Sala sala = salaService.getSala(id);
        if (sala == null) {
            return "redirect:/proiezioni";
        }
        model.addAttribute("sala", sala);
        model.addAttribute("proiezioni", proiezioneService.getProiezioniBySala(sala));
        return "sala/detail";
    }

    @GetMapping("/sale/nuova")
    public String showCreateSalaForm(Model model) {
        model.addAttribute("sala", new Sala());
        return "sala/form";
    }

    @PostMapping("/sale/salva")
    public String saveSala(@Valid @ModelAttribute("sala") Sala sala,
                          BindingResult bindingResult,
                          Model model) {
        salaValidator.validate(sala, bindingResult);

        if (bindingResult.hasErrors()) {
            return "sala/form";
        }

        salaService.saveSala(sala);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/sale/modifica/{id}")
    public String showEditSalaForm(@PathVariable("id") Long id, Model model) {
        Sala sala = salaService.getSala(id);
        if (sala == null) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("sala", sala);
        return "sala/form";
    }

    @PostMapping("/sale/{id}/elimina")
    public String deleteSala(@PathVariable("id") Long id) {
        salaService.deleteSala(id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/sale/elimina/{id}")
    public String deleteSalaGet(@PathVariable("id") Long id) {
        salaService.deleteSala(id);
        return "redirect:/admin/dashboard";
    }
}
