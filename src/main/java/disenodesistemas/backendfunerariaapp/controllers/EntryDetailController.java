package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.EntryDetailCreationDto;
import disenodesistemas.backendfunerariaapp.dto.EntryDetailDto;
import disenodesistemas.backendfunerariaapp.models.requests.EntryDetailRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.EntryDetailRest;
import disenodesistemas.backendfunerariaapp.models.responses.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.EntryDetailService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/entryDetails")
public class EntryDetailController {

    @Autowired
    EntryDetailService entryDetailService;

    @Autowired
    ModelMapper mapper;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public EntryDetailRest createEntryDetail(@RequestBody @Valid EntryDetailRequestModel entryDetailRequestModel) {
        EntryDetailCreationDto entryDetailCreationDto = mapper.map(entryDetailRequestModel, EntryDetailCreationDto.class);
        EntryDetailDto entryDetailDto = entryDetailService.createEntryDetail(entryDetailCreationDto);
        return mapper.map(entryDetailDto, EntryDetailRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}")
    public EntryDetailRest updateEntryDetail(@PathVariable long id, @Valid @RequestBody EntryDetailRequestModel entryDetailRequestModel) {
        EntryDetailDto entryDetailDto = entryDetailService.updateEntryDetail(id, mapper.map(entryDetailRequestModel, EntryDetailCreationDto.class));
        return mapper.map(entryDetailDto, EntryDetailRest.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteEntryDetail(@PathVariable long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        entryDetailService.deleteEntryDetail(id);
        operationStatusModel.setResult("SUCCESS");
        return operationStatusModel;
    }



}
