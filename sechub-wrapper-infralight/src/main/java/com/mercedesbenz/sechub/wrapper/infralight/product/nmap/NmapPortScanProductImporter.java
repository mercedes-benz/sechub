package com.mercedesbenz.sechub.wrapper.infralight.product.nmap;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanFinding;
import com.mercedesbenz.sechub.wrapper.infralight.product.InfralightProductImporter;

/**
 * The class is called not NmapProdcutIMporter but NmapPortScanProductImporter
 * because here we read only port scan information.
 */
@Component
public class NmapPortScanProductImporter implements InfralightProductImporter {

    private static final Logger LOG = LoggerFactory.getLogger(NmapPortScanProductImporter.class);

    /* @formatter:off */
    private static final List<Integer> typicallyOpenPorts = List.of(
            80, // HTTP
            443 // HTTPS
            
            ); 
    private static final List<Integer> restrictedPorts = List.of(
            22,   // SSH
            23,   // Telnet
            25,   // SMTP
            135,  // NetBIOS
            136,  // NetBIOS
            137,  // NetBIOS
            138,  // NetBIOS
            139,  // NetBIOS
            445,  // SMB
            1433, // Microsoft SQL Server
            3306, // MySQL
            3389, // RDP
            5900, // VNC
            6379  // Redis
        ); 
    /* @formatter:on */
    
    @Override
    public List<GenericInfrascanFinding> startImport(String data) {

        List<GenericInfrascanFinding> list = new ArrayList<>();

        try {
            SAXReader xmlReader = new SAXReader();
//            xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            
            Document document = xmlReader.read(new ByteArrayInputStream(data.getBytes()));
            
            List<Element> hosts = document.getRootElement().elements("host");
            for (Element host : hosts) {
                Element addressElement = host.element("address");
                String ipAddress = addressElement.attributeValue("addr");

                Element portsElement = host.element("ports");
                List<Element> ports = portsElement.elements("port");
                for (Element port : ports) {
                    String portId = port.attributeValue("portid");
                    Integer portIdInt = null;
                    try {
                        portIdInt = Integer.valueOf(portId);
                    }catch(NumberFormatException e) {
                        LOG.error("Was not able to read port to integer:"+portId,e);
                    }
                    String protocol = port.attributeValue("protocol");
                    Element serviceElement = port.element("service");
                    String serviceName = serviceElement != null ? serviceElement.attributeValue("name") : "Unknown";
                    GenericInfrascanFinding finding = new GenericInfrascanFinding();
                    finding.setName("Open port detected");
                    finding.setDescription("Detected open "+protocol+" port "+portId + " (Service: " + serviceName + ") on IP:"+ipAddress);
                    
                    if (isTypicalOpenedPort(portIdInt)) {
                        finding.setSeverity(Severity.INFO);
                    }else if (isRestricted(portIdInt)) {
                        finding.setSeverity(Severity.HIGH);
                    }else {
                        finding.setSeverity(Severity.MEDIUM);
                    }
                    list.add(finding);
                }
            }
        } catch (DocumentException | SAXException e) {
            LOG.error("Was not able to import xml file",e);
        }

        String json = JSONConverter.get().toJSON(list,true);
        System.out.println(json);
        return list;
    }

    private boolean isTypicalOpenedPort(Integer portId) {
        return typicallyOpenPorts.contains(portId);
    }
    
    private boolean isRestricted(Integer portId) {
        return restrictedPorts.contains(portId);
    }

    @Override
    public String getProductName() {
        return "nmap-portscan";
    }

    @Override
    public String getImportFileName() {
        return "nmap_portscan-output.xml";
    }

}
