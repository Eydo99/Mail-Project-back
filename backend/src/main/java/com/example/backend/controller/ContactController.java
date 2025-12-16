package com.example.backend.controller;

import com.example.backend.DTOS.contactRequestDTO;
import com.example.backend.DTOS.contactResponseDTO;
import com.example.backend.DTOS.PaginatedContactResponse;
import com.example.backend.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import static com.example.backend.controller.FolderController.getStringMap;


@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ContactController {

    @Autowired
    private ContactService service;

    /**
     * helper method to get current loggedIn user from local storage
     * @param request : the HTTP servlet request
     * @return loggedIn email in local storage
     */
    private String getLoggedInUser(HttpServletRequest request) {
        String email = (String) request.getSession().getAttribute("currentUser");
        System.out.println("ContactController - Getting logged in user: " + email);
        System.out.println("Session ID: " + request.getSession().getId());
        return email;
    }


    /**
     * get all contacts
     * @param page : current page
     * @param size : current no of contacts
     * @param search : search criteria
     * @param sortBy : sortBy criteria
     * @param request : the HTTP servlet request
     * @return list of contacts
     */

    @GetMapping
    public PaginatedContactResponse getContacts(@RequestParam int page,
                                                @RequestParam int size,
                                                @RequestParam(required = false) String search,
                                                @RequestParam(defaultValue = "name") String sortBy,
                                                HttpServletRequest request) {
        return service.getContacts(getLoggedInUser(request), page, size, search, sortBy);
    }

    /**
     * creates a new contact
     * @param dto : data of the new contact
     * @param request : the HTTP servlet request
     * @return contact
     */
    @PostMapping
    public ResponseEntity<contactResponseDTO> addContact(@Valid @RequestBody contactRequestDTO dto,
                                                         HttpServletRequest request) {
        contactResponseDTO response = service.addContact(getLoggedInUser(request), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * update an exisitn contact
     * @param id : id of contact to be updated
     * @param dto :the updated data
     * @param request: the HTTP servlet request
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateContact(@PathVariable String id,
                                              @Valid @RequestBody contactRequestDTO dto,
                                              HttpServletRequest request) {
        service.updateContact(getLoggedInUser(request), id, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * delete a contact
     * @param id : id of contact to be deleted
     * @param request :the HTTP servlet request
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable String id,
                                              HttpServletRequest request) {
        service.deleteContact(getLoggedInUser(request), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * handles @valid when the argument is not valid
     * @param ex : the exception to be  handled
     * @return map ot the exceptions that need to be handled
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return getStringMap(ex);
    }
}