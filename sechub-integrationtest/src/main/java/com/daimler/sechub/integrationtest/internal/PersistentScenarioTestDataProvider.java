// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Provides persistent test data for integration tests - e.g. growing ids for test scenarios.
 * 
 * @author Albert Tregnaghi
 *
 */
public class PersistentScenarioTestDataProvider {

    private static final String BASE_SECHUB_INTEGRATIONTEST_DATA_KEY = "sechub.integrationtest.data.";
    private static final String SECHUB_INTEGRATIONTEST_DATA_GROWINGID = BASE_SECHUB_INTEGRATIONTEST_DATA_KEY+"growingid";
    private int grow;
    File file;
    private Properties properties;

    public PersistentScenarioTestDataProvider(GrowingScenario scenario) {
        this(new File(IntegrationTestFileSupport.getTestfileSupport().getIntegrationTestDataFolder(),
                "scenario-testdata_" + scenario.getClass().getSimpleName().toLowerCase() + ".properties"));
    }

    /**
     * @param file
     */
    public PersistentScenarioTestDataProvider(File file) {
        this.file = file;
        this.file.getParentFile().mkdirs();
        properties = new Properties();
        if (this.file.exists()) {

            try (FileInputStream fis =  new FileInputStream(file)) {
                properties.load(fis);
                String d = properties.getProperty(SECHUB_INTEGRATIONTEST_DATA_GROWINGID);
                if (d==null) {
                    grow = 0;
                }else {
                    grow = Integer.parseInt(d);
                }
            } catch (Exception e) {
                this.file.delete();
                throw new IllegalStateException("cannot read growid file: " + file.getAbsolutePath()+", so deleted as fallback",e);
            }
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            }catch(IOException e) {
                throw new IllegalStateException("cannot create growid file:" + file.getAbsolutePath(),e);
            }
        }

    }

    /**
     * Increases grow id and stores updated data automatically to file system
     * @throws IllegalStateException when grow id becomes greater than 9999 because names would 
     * become to big for generated user names etc. 
     */
    public void increaseGrowId() {
        grow++;
        if (grow > 9999) {
            throw new IllegalStateException("Grow ID >9999 - not valid");
        }
        properties.put(SECHUB_INTEGRATIONTEST_DATA_GROWINGID, ""+grow);
        store();
        
    }

    private void store() {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos,
                    "DO NOT CHANGE THIS FILE!");
        } catch (IOException e) {
            throw new IllegalStateException("cannot store:" + file.getAbsolutePath());
        }
    }

    public String getGrowId() {
        StringBuilder sb = new StringBuilder();
        sb.append(grow);
        while (sb.length() < 4) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }

}
