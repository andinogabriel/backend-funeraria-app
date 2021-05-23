package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.ReceiptTypeDto;
import disenodesistemas.backendfunerariaapp.models.responses.ReceiptTypeRest;
import disenodesistemas.backendfunerariaapp.service.ReceiptTypeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/receiptTypes")
public class ReceiptTypeController {

    @Autowired
    ReceiptTypeService receiptTypeService;

    @Autowired
    ModelMapper mapper;

    @GetMapping
    public List<ReceiptTypeRest> getAllReceiptTypes() {
        List<ReceiptTypeDto> receiptTypesDto = receiptTypeService.getAllReceiptTypes();
        List<ReceiptTypeRest> receiptTypesRest = new ArrayList<>();
        receiptTypesDto.forEach(r -> receiptTypesRest.add(mapper.map(r, ReceiptTypeRest.class)));
        return receiptTypesRest;
    }

}
