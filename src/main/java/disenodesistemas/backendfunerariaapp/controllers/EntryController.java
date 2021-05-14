package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.EntryCreationDto;
import disenodesistemas.backendfunerariaapp.dto.EntryDto;
import disenodesistemas.backendfunerariaapp.models.requests.EntryRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.EntryRest;
import disenodesistemas.backendfunerariaapp.service.EntryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/entries")
public class EntryController {

    @Autowired
    EntryService entryService;

    @Autowired
    ModelMapper mapper;

    @GetMapping
    public Page<EntryRest> getEntriesPaginated(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "5") int limit, @RequestParam(value = "sortBy", defaultValue = "entryDate") String sortBy, @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Page<EntryDto> entriesDto = entryService.getEntriesPaginated(page, limit, sortBy, sortDir);
        return mapper.map(entriesDto, Page.class);
    }

    @PostMapping
    public EntryRest createEntry(@RequestBody @Valid EntryRequestModel entryRequestModel) {
        EntryCreationDto entryCreationDto = mapper.map(entryRequestModel, EntryCreationDto.class);
        EntryDto entryDto = entryService.createEntry(entryCreationDto);
        return mapper.map(entryDto, EntryRest.class);
    }





}
