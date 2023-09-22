package com.cloudslip.pipeline.updated.service;

import com.cloudslip.pipeline.updated.dto.GetLogInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.helper.app_log.LogFetchHelper;
import com.cloudslip.pipeline.updated.model.universal.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jws.soap.SOAPBinding;

@Service
@Transactional
public class LogFetchService {

    private final Logger log = LoggerFactory.getLogger(AppEnvironmentService.class);

    @Autowired
    private LogFetchHelper logFetchHelper;

    public ResponseDTO fetchLog(GetLogInputDTO getLogInputDTO, User requester) {
        log.debug("REST request to fetch log : {}", getLogInputDTO);
        return (ResponseDTO) logFetchHelper.execute(getLogInputDTO, requester, null);
    }
}
