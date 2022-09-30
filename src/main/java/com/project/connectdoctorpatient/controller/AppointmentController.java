package com.project.connectdoctorpatient.controller;

import com.project.connectdoctorpatient.model.Appointment;
import com.project.connectdoctorpatient.util.RedirectUtil;
import com.project.connectdoctorpatient.exception.AccessDeniedException;
import com.project.connectdoctorpatient.exception.NotFoundException;
import com.project.connectdoctorpatient.helper.AppointmentHelper;
import com.project.connectdoctorpatient.model.Action;
import com.project.connectdoctorpatient.service.AppointmentService;
import com.project.connectdoctorpatient.service.AccessManager;
import com.project.connectdoctorpatient.validator.AppointmentValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.project.connectdoctorpatient.constant.URL.*;
import static com.project.connectdoctorpatient.controller.AppointmentController.APPOINTMENT_CMD;
import static com.project.connectdoctorpatient.model.Action.SHOW;

/**
 * @author sakib.khan
 * @since 2/26/22
 */
@Controller
@RequestMapping("/appointment")
@SessionAttributes(APPOINTMENT_CMD)
public class AppointmentController {

    public static final String APPOINTMENT_CMD = "appointment";

    private static final String VIEW_PAGE = "appointment/appointment";
    private static final String LIST_PAGE = "appointment/appointmentList";

    @Autowired
    private AccessManager accessManager;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentHelper appointmentHelper;

    @Autowired
    private AppointmentValidator appointmentValidator;

    @InitBinder(APPOINTMENT_CMD)
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
        binder.addValidators(appointmentValidator);
        binder.setDisallowedFields("id");
    }

    @GetMapping
    public String show(@RequestParam(defaultValue = "0") long appointmentId,
                       @RequestParam(defaultValue = "0") long issueId,
                       ModelMap model) throws AccessDeniedException, NotFoundException {

        accessManager.checkAccess(SHOW);

        appointmentHelper.setupReferenceData(issueId, appointmentId, model);

        return VIEW_PAGE;
    }

    @PostMapping
    public String process(@Valid @ModelAttribute(APPOINTMENT_CMD) Appointment appointment,
                          Errors errors,
                          @RequestParam Action action,
                          SessionStatus sessionStatus,
                          RedirectAttributes redirectAttributes) throws AccessDeniedException {

        accessManager.checkAccess(action);

        if (errors.hasErrors()) {
            return VIEW_PAGE;
        }

        appointmentService.performAction(action, appointment);

        sessionStatus.setComplete();

        appointmentHelper.setupSuccessMessage(action, redirectAttributes);

        return RedirectUtil.redirect(SUCCESS_PAGE_URL);
    }

    @GetMapping(value = "/list")
    public String showAll(ModelMap model) throws AccessDeniedException {
        accessManager.checkAccess(SHOW);

        appointmentHelper.setupList(model);

        return LIST_PAGE;
    }
}
