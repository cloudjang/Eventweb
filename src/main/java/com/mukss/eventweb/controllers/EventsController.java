package com.mukss.eventweb.controllers;

import java.time.LocalDateTime;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mukss.eventweb.config.userdetails.CustomUserDetails;
import com.mukss.eventweb.entities.Event;
import com.mukss.eventweb.entities.User;
import com.mukss.eventweb.exceptions.EventNotFoundException;
import com.mukss.eventweb.services.EventService;

@Controller
@RequestMapping(value = "/events", produces = MediaType.TEXT_HTML_VALUE)
public class EventsController {
	
	@Autowired
	private EventService eventService;
	
	// TODO: Add not_found error
	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String EventNotFoundHandler(EventNotFoundException ex, Model model){
		model.addAttribute("not_found_id", ex.getId());
		
		return "events/not_found";
	}
	
	// Returns all events as a list, under attribute "posts" of model
	@GetMapping
	public String getEvents(Model model) {
		model.addAttribute("posts", eventService.findAll());
		return "events/index";
	}
	
	// Returns event with given event ID, under attribute event
	@GetMapping("/{id}")
	public String getEvent(@PathVariable("id") long id,
			@RequestParam(value = "name", required = false, defaultValue = "World") String name, Model model) {
		
		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		
		// attend 추가
		
		model.addAttribute("event", event);
		
		return "events/view";
		
	}
	
	// Adding new event
	@GetMapping("/new")
	public String newEvent(Model model) {
		// if model does not have event, initialize a new event
		if (!model.containsAttribute("event")) {
			model.addAttribute("event", new Event());
		}
		return "events/new";
	}
	
	@PostMapping(value = "/new", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createEvent(@RequestBody @Valid @ModelAttribute Event event, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) {
		
		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			return "/events/new";
		}
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = null;
		if (principal instanceof CustomUserDetails) {
			user = ((CustomUserDetails)principal).getUser();
		} 		
		
		event.setTimeUploaded(LocalDateTime.now());
		
		// save post after automatically adding relevant meta info
		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "New event added.");

		return "redirect:/events";
	}
		
		
	// Update new event
	@GetMapping("/update/{id}")
	public String updateEvent(@PathVariable("id") long id, Model model) {
		// if model does not have event, initialize a new event
		if (!model.containsAttribute("event")) {
			model.addAttribute("event", new Event());
		}
		// load event by id
		Event event = eventService.findById(id).orElseThrow(()-> new EventNotFoundException(id));
		model.addAttribute("event", event);
		
		return "events/update";
	}
	
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String updateEvent(@RequestBody @Valid @ModelAttribute Event event, BindingResult errors,
			Model model, RedirectAttributes redirectAttrs) {
		
		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			return "/events/update";
		}
		
		// save update event info
		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "New event added: ");
		return "redirect:/events";
	}
	
	//delete one greeting
	@DeleteMapping("/{id}")
	public String deletePost(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		eventService.deleteById(id);
		redirectAttrs.addFlashAttribute("ok_message", "Event deleted.");
		return "redirect:/events";
	}
	
	
	
}