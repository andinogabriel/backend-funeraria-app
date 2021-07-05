package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.EntryCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.EntryResponseDto;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.Interface.IEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/v1/entries")
public class EntryController {

    private final IEntry entryService;

    @Autowired
    public EntryController(IEntry entryService) {
        this.entryService = entryService;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<EntryResponseDto> getEntries() {
        return entryService.getAllEntries();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public EntryResponseDto getEntryById(@PathVariable long id) {
        return entryService.getProjectedEntryById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/paginated")
    public Page<EntryResponseDto> getEntriesPaginated(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "5") int limit, @RequestParam(value = "sortBy", defaultValue = "entryDate") String sortBy, @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        return entryService.getEntriesPaginated(page, limit, sortBy, sortDir);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public EntryResponseDto createEntry(@RequestBody @Valid EntryCreationDto entryCreationDto) {
        //con SecurityContextHolder accedemos al contexto de la parte de la seguridad de la app y obtenemos la autenticacion del user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Del metodo obtenemos el subject name que seria nuestro email
        String email = authentication.getName();
        entryCreationDto.setEntryUser(email);
        return entryService.createEntry(entryCreationDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}")
    public EntryResponseDto updateEntry(@PathVariable long id ,@Valid @RequestBody EntryCreationDto entryCreationDto) {
        return entryService.updateEntry(id, entryCreationDto);
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
