package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.EntryDetailCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.EntryDetailResponseDto;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.Interface.IEntryDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/entryDetails")
public class EntryDetailController {

    private final IEntryDetail entryDetailService;

    @Autowired
    public EntryDetailController(IEntryDetail entryDetailService) {
        this.entryDetailService = entryDetailService;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public EntryDetailResponseDto createEntryDetail(@RequestBody @Valid EntryDetailCreationDto entryDetailCreationDto) {
       return entryDetailService.createEntryDetail(entryDetailCreationDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}")
    public EntryDetailResponseDto updateEntryDetail(@PathVariable long id, @Valid @RequestBody EntryDetailCreationDto entryDetailCreationDto) {
        return entryDetailService.updateEntryDetail(id, entryDetailCreationDto);
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
