package com.lssgoo.mail.service;

import com.lssgoo.mail.utils.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MailDnsService {

    private static final Logger logger = LoggerUtil.getLogger(MailDnsService.class);

    @Value("${mail.server.domain:lssgoo.com}")
    private String mailDomain;

    @Value("${mail.server.ip:}")
    private String mailServerIp;

    public Map<String, String> getDnsRecords() {
        logger.info("Getting DNS records for domain: {}", mailDomain);
        
        Map<String, String> dnsRecords = new HashMap<>();
        
        // SPF Record
        String spfRecord = String.format("v=spf1 mx a ip4:%s ~all", 
                mailServerIp != null && !mailServerIp.isEmpty() ? mailServerIp : "YOUR_SERVER_IP");
        dnsRecords.put("SPF", spfRecord);
        dnsRecords.put("SPF_Type", "TXT");
        dnsRecords.put("SPF_Host", "@");
        
        // DKIM Record (placeholder - should be fetched from mail server)
        dnsRecords.put("DKIM", "v=DKIM1; k=rsa; p=YOUR_DKIM_PUBLIC_KEY");
        dnsRecords.put("DKIM_Type", "TXT");
        dnsRecords.put("DKIM_Host", "default._domainkey");
        
        // DMARC Record
        String dmarcRecord = "v=DMARC1; p=quarantine; rua=mailto:dmarc@lssgoo.com; ruf=mailto:dmarc@lssgoo.com; sp=quarantine; aspf=r;";
        dnsRecords.put("DMARC", dmarcRecord);
        dnsRecords.put("DMARC_Type", "TXT");
        dnsRecords.put("DMARC_Host", "_dmarc");
        
        // MX Record
        dnsRecords.put("MX", String.format("10 mail.%s", mailDomain));
        dnsRecords.put("MX_Type", "MX");
        dnsRecords.put("MX_Host", "@");
        
        logger.info("DNS records generated for domain: {}", mailDomain);
        return dnsRecords;
    }

    public Map<String, Object> getDnsStatus() {
        logger.info("Getting DNS status for domain: {}", mailDomain);
        
        Map<String, Object> status = new HashMap<>();
        status.put("domain", mailDomain);
        status.put("serverIp", mailServerIp);
        status.put("records", getDnsRecords());
        status.put("status", "configured");
        
        return status;
    }
}

