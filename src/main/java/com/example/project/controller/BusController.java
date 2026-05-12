package com.example.project.controller;
import com.example.project.dto.BusDTO;
import com.example.project.service.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/buses")
@RequiredArgsConstructor
public class BusController {
    private final BusService busService;
    @GetMapping
    public String listBuses(Model model) {
        model.addAttribute("buses", busService.getAllBuses());
        model.addAttribute("newBus", new BusDTO());
        return "admin/buses";
    }
    @PostMapping
    public String createBus(@ModelAttribute BusDTO busDTO, RedirectAttributes redirectAttributes) {
        if (busService.isPlateNumberExists(busDTO.getPlateNumber())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Biển số xe \"" + busDTO.getPlateNumber() + "\" đã tồn tại. Vui lòng nhập biển số khác!");
            redirectAttributes.addFlashAttribute("newBus", busDTO);
            return "redirect:/admin/buses";
        }
        busService.createBus(busDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm xe mới thành công!");
        return "redirect:/admin/buses";
    }
    @GetMapping("/edit/{id}")
    public String editBusForm(@PathVariable Long id, Model model) {
        model.addAttribute("bus", busService.getBusById(id));
        return "admin/bus-edit";
    }
    @PostMapping("/edit/{id}")
    public String updateBus(@PathVariable Long id, @ModelAttribute BusDTO busDTO) {
        busService.updateBus(id, busDTO);
        return "redirect:/admin/buses";
    }
    @GetMapping("/delete/{id}")
    public String deleteBus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            busService.deleteBus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa xe thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/buses";
    }
}

