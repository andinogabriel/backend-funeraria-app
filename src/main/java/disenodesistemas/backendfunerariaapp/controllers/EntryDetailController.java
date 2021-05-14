package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.EntryCreationDto;
import disenodesistemas.backendfunerariaapp.dto.EntryDetailCreationDto;
import disenodesistemas.backendfunerariaapp.dto.EntryDetailDto;
import disenodesistemas.backendfunerariaapp.models.requests.EntryDetailRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.EntryDetailRest;
import disenodesistemas.backendfunerariaapp.service.EntryDetailService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/entryDetails")
public class EntryDetailController {

    @Autowired
    EntryDetailService entryDetailService;

    @Autowired
    ModelMapper mapper;

    @PostMapping
    public EntryDetailRest createEntryDetail(@RequestBody @Valid EntryDetailRequestModel entryDetailRequestModel) {
        EntryDetailCreationDto entryDetailCreationDto = mapper.map(entryDetailRequestModel, EntryDetailCreationDto.class);
        EntryDetailDto entryDetailDto = entryDetailService.createEntryDetail(entryDetailCreationDto);
        return mapper.map(entryDetailDto, EntryDetailRest.class);
    }



}
