package com.daimler.sechub.domain.scan.project;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.daimler.sechub.commons.model.JSONConverter;

class FalsePositiveWebMetaDataTest {

    
    @Test 
    void meta_data_object_can_be_transformed_to_json_and_back() {
        /* prepare*/
        FalsePositiveWebMetaData data1 = createExampleMetaData();
        
        /* execute*/
        String json = JSONConverter.get().toJSON(data1);
        FalsePositiveWebMetaData data2 = JSONConverter.get().fromJSON(FalsePositiveWebMetaData.class, json);
        
        /* test */
        assertEquals(data1,data2);
    }
    
    @Test
    void empty_objects_are_equal() {
        /* prepare*/
        FalsePositiveWebMetaData data1 = new FalsePositiveWebMetaData();
        FalsePositiveWebMetaData data2 = new FalsePositiveWebMetaData();
        
        /* execute + test */
        assertEquals(data1,data2);
    }
    
    @Test
    void same_meta_data_so_equal() {
        /* prepare*/
        FalsePositiveWebMetaData data1 = createExampleMetaData();
        FalsePositiveWebMetaData data2 = createExampleMetaData();
        
        /* execute + test */
        assertEquals(data1,data2);
    }
    
    @Test
    void same_meta_data_except_target_so_NOT_equal() {
        /* prepare*/
        FalsePositiveWebMetaData data1 = createExampleMetaData();
        
        FalsePositiveWebMetaData data2 = createExampleMetaData();
        data2.getRequest().setTarget(change(data2.getRequest().getTarget()));
        
        /* execute + test */
        assertNotEquals(data1,data2);
    }
    
    @Test
    void same_meta_data_except_method_so_NOT_equal() {
        /* prepare*/
        FalsePositiveWebMetaData data1 = createExampleMetaData();
        
        FalsePositiveWebMetaData data2 = createExampleMetaData();
        data2.getRequest().setMethod(change(data2.getRequest().getMethod()));
        
        /* execute + test */
        assertNotEquals(data1,data2);
    }
    
    @Test
    void same_meta_data_except_attack_vector_so_NOT_equal() {
        /* prepare*/
        FalsePositiveWebMetaData data1 = createExampleMetaData();
        
        FalsePositiveWebMetaData data2 = createExampleMetaData();
        data2.getRequest().setAttackVector(change(data2.getRequest().getAttackVector()));
        
        /* execute + test */
        assertNotEquals(data1,data2);
    }
    
    @Test
    void same_meta_data_except_protocol_so_NOT_equal() {
        /* prepare*/
        FalsePositiveWebMetaData data1 = createExampleMetaData();
        
        FalsePositiveWebMetaData data2 = createExampleMetaData();
        data2.getRequest().setProtocol(change(data2.getRequest().getProtocol()));
        
        /* execute + test */
        assertNotEquals(data1,data2);
    }
    
    @Test
    void same_meta_data_except_version_so_NOT_equal() {
        /* prepare*/
        FalsePositiveWebMetaData data1 = createExampleMetaData();
        
        FalsePositiveWebMetaData data2 = createExampleMetaData();
        data2.getRequest().setVersion(change(data2.getRequest().getVersion()));
        
        /* execute + test */
        assertNotEquals(data1,data2);
    }
    
    @Test
    void same_meta_data_except_evidence_so_NOT_equal() {
        /* prepare*/
        FalsePositiveWebMetaData data1 = createExampleMetaData();
        
        FalsePositiveWebMetaData data2 = createExampleMetaData();
        data2.getResponse().setEvidence(change(data2.getResponse().getEvidence()));
        
        /* execute + test */
        assertNotEquals(data1,data2);
    }
    
    @Test
    void same_meta_data_except_statuscode_so_NOT_equal() {
        /* prepare*/
        FalsePositiveWebMetaData data1 = createExampleMetaData();
        
        FalsePositiveWebMetaData data2 = createExampleMetaData();
        data2.getResponse().setStatusCode(data2.getResponse().getStatusCode()+123);
        
        /* execute + test */
        assertNotEquals(data1,data2);
    }
    
    private String change(String data) {
        return data+"_other";
    }

    private FalsePositiveWebMetaData createExampleMetaData() {
        FalsePositiveWebMetaData data = new FalsePositiveWebMetaData();
        FalsePositiveWebRequestMetaData request = data.getRequest();
        request.setMethod("GET");
        request.setTarget("https://example.com/same-targeturl");
        request.setAttackVector("attack1");
        request.setProtocol("HTTPS");
        request.setVersion("1.1");
        
        FalsePositiveWebResponseMetaData response = data.getResponse();
        response.setEvidence("evidence1");
        response.setStatusCode(400);
        
        return data;
    }
    
}
