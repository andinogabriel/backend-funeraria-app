package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.response.ReceiptTypeResponseDto;
import disenodesistemas.backendfunerariaapp.service.impl.ReceiptTypeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/receiptTypes")
public class ReceiptTypeController {

    private final ReceiptTypeServiceImpl receiptTypeServiceImpl;

    @Autowired
    public ReceiptTypeController(ReceiptTypeServiceImpl receiptTypeServiceImpl) {
        this.receiptTypeServiceImpl = receiptTypeServiceImpl;
    }

    @GetMapping
    public List<ReceiptTypeResponseDto> getAllReceiptTypes() {
        return receiptTypeServiceImpl.getAllReceiptTypes();
    }

}
