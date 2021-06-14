package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.EntryCreationDto;
import disenodesistemas.backendfunerariaapp.dto.EntryDto;
import disenodesistemas.backendfunerariaapp.models.requests.EntryRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.EntryRest;
import disenodesistemas.backendfunerariaapp.models.responses.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.EntryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1/entries")
public class EntryController {

    @Autowired
    EntryService entryService;

    @Autowired
    ModelMapper mapper;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<EntryRest> getEntries() {
        List<EntryDto> entriesDto = entryService.getAllEntries();
        List<EntryRest> entriesRest = new ArrayList<>();
        entriesDto.forEach(e -> entriesRest.add(mapper.map(e, EntryRest.class)));
        return entriesRest;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public EntryRest getEntryById(@PathVariable long id) {
        return mapper.map(entryService.getEntryById(id), EntryRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/paginated")
    public Page<EntryRest> getEntriesPaginated(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "5") int limit, @RequestParam(value = "sortBy", defaultValue = "entryDate") String sortBy, @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Page<EntryDto> entriesDto = entryService.getEntriesPaginated(page, limit, sortBy, sortDir);
        return mapper.map(entriesDto, Page.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public EntryRest createEntry(@RequestBody @Valid EntryRequestModel entryRequestModel) {
        //con SecurityContextHolder accedemos al contexto de la parte de la seguridad de la app y obtenemos la autenticacion del user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Del metodo obtenemos el subject name que seria nuestro email
        String email = authentication.getName();
        EntryCreationDto entryCreationDto = mapper.map(entryRequestModel, EntryCreationDto.class);
        entryCreationDto.setEntryUser(email);
        EntryDto entryDto = entryService.createEntry(entryCreationDto);
        return mapper.map(entryDto, EntryRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}")
    public EntryRest updateEntry(@PathVariable long id ,@Valid @RequestBody EntryRequestModel entryRequestModel) {
        EntryDto entryDto = entryService.updateEntry(id, mapper.map(entryRequestModel, EntryCreationDto.class));
        return mapper.map(entryDto, EntryRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteEntry(@PathVariable long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        entryService.deleteEntry(id);
        operationStatusModel.setResult("SUCCESS");
        return operationStatusModel;
    }

}
