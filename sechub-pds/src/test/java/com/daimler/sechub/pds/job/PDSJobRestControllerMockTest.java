// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.daimler.sechub.pds.PDSProfiles;
import com.daimler.sechub.pds.security.AbstractAllowPDSAPISecurityConfiguration;
import com.daimler.sechub.pds.security.PDSRoleConstants;
import com.daimler.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(PDSJobRestController.class)
/* @formatter:off */
@ContextConfiguration(classes = { 
        PDSJobRestController.class,
        PDSJobTransactionService.class,
        PDSFileUploadJobService.class,
        PDSCreateJobService.class, 
        PDSGetJobResultService.class, 
        PDSCancelJobService.class, 
        PDSGetJobStatusService.class,
		PDSJobRestControllerMockTest.SimpleTestConfiguration.class })
/* @formatter:on */
@WithMockUser(authorities = PDSRoleConstants.ROLE_USER)
@ActiveProfiles(PDSProfiles.TEST)
public class PDSJobRestControllerMockTest {

	private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();
	
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PDSCreateJobService mockedCreateService;
	
	@MockBean
    private PDSGetJobStatusService mockedJobStatusService;
	
	@MockBean
    private PDSGetJobResultService mockedJobResultService;
	
	@MockBean
    private PDSFileUploadJobService mockedFileUploadJobService;
	
	@MockBean
	private PDSJobTransactionService mockedMarkReadyToStartJobService;
	
	@MockBean
	private PDSCancelJobService mockedCancelJobService;


	@Test
	public void a_job_create_call_calls_creation_service_and_returns_result() throws Exception {
		/* prepare */
		UUID sechubJobUUID = UUID.randomUUID();
		
        /* execute */
		/* @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).pds().buildCreateJob()).
        			contentType(MediaType.APPLICATION_JSON_VALUE).
        			content("{\"apiVersion\":\"1.0\",\"sechubJobUUID\":\""+sechubJobUUID.toString()+"\"}")
        		).
        			andExpect(status().isOk()
        		);

        /* @formatter:on */

        /* test */
        ArgumentCaptor<PDSJobConfiguration> configurationCaptor = ArgumentCaptor.forClass(PDSJobConfiguration.class);
		verify(mockedCreateService).createJob(configurationCaptor.capture());
		
		PDSJobConfiguration configuration = configurationCaptor.getValue();
        assertEquals("1.0", configuration.getApiVersion());
		assertEquals(sechubJobUUID, configuration.getSechubJobUUID());
	}
	
	@Test
    public void a_get_job_status_call_calls_status_service_and_returns_status_as_JSON() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        
        PDSJobStatus status = new PDSJobStatus();
        status.created="created1";
        status.ended="ended1";
        status.jobUUID=jobUUID;
        status.owner="owner1";
        status.state="state1";
        
        when(mockedJobStatusService.getJobStatus(jobUUID)).thenReturn(status);
        
        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).pds().buildGetJobStatus(jobUUID))
                ).
                    andExpect(status().isOk()).
                    andExpect(content().json(status.toJSON(),true)
                );

        /* @formatter:on */

    }
	
	@Test
    public void a_get_job_result_call_calls_result_service_and_returns_result_string() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        
        String result = "result string";
        when(mockedJobResultService.getJobResult(jobUUID)).thenReturn(result);
        
        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).pds().buildGetJobResult(jobUUID))
                ).
                    andExpect(status().isOk()).
                    andExpect(content().string(result)
                );

        /* @formatter:on */

    }
	
	@Test
    public void a_mark_job_ready_call_calls_mark_ready_service_and_returns_ok() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        
        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(
                put(https(PORT_USED).pds().buildMarkJobReadyToStart(jobUUID))
                ).
                    andExpect(status().isOk()
                );

        /* @formatter:on */
        
        verify(mockedMarkReadyToStartJobService).markReadyToStartInOwnTransaction(jobUUID);

    }
	
	@Test
    public void a_cancel_job_call_calls_canceljob_service_returns_ok() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        
        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(
                put(https(PORT_USED).pds().buildCancelJob(jobUUID))
                ).
                    andExpect(status().isOk()
                );

        /* @formatter:on */
        
        verify(mockedCancelJobService).cancelJob(jobUUID);

    }
	
	@Test
    public void an_upload__call_calls_upload_file_service_and_returns_ok() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        String result = "result string";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "sourcecode.zip";
        
        /* execute + test */
        
        /* @formatter:off */
        this.mockMvc.perform(
                multipart(https(PORT_USED).pds().buildUpload(jobUUID,fileName)).
                file(multiPart).
                
                param("checkSum", "mychecksum")
                ).
                    andExpect(status().isOk()
                );

        verify(mockedFileUploadJobService).upload(jobUUID,fileName, multiPart, "mychecksum");
        /* @formatter:on */

    }


	@TestConfiguration
	@Profile(PDSProfiles.TEST)
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowPDSAPISecurityConfiguration {

	}

}
