import { ProjectData } from 'sechub-openapi-ts-client';
import { DefaultClient } from '../api/defaultClient';
import { SECHUB_REPORT_KEYS } from '../utils/sechubConstants';
import * as vscode from 'vscode';

export class JobListTable {

    private noJobsRunnedMessage = '<p>No jobs have been started for your project or you are not allowed to view them.</p>';
    private failedToFetchJobsMessage = '<p>Failed to retrieve job list. Please check your project selection and connection.</p>';
    
    public async createJobListTable(context: vscode.ExtensionContext) : Promise<string> {

        const project: ProjectData | undefined = context.globalState.get(SECHUB_REPORT_KEYS.selectedProject);
        if (!project) {
            return this.noJobsRunnedMessage;
        }
        const projectId = project.projectId;
        const title = `<p>SecHub Job List for Project: ${projectId}</p>`;

        const client = await DefaultClient.getInstance(context);
        const data = await client.userListsJobsForProject(projectId);
        if(data){
            if(data.content && data.content.length > 0) {
                const haeder  = `
                <thead>
                    <tr>
                        <th>Created</th>
                        <th>Status</th>
                        <th>Result</th>
                        <th>Traffic Light</th>
                        <th>Job UUID</th>
                        <th>Executed By</th>
                    </tr>
                    <thead>`;

                let tableContent = '';
                data.content.forEach(job => {
                    const date = this.formatDate(job.created + '');
                    tableContent += `<tbody>
                                <tr>
                                    <td>${date}</td>
                                    <td>${job.executionState}</td>
                                    <td>${job.executionResult}</td>
                                    <td>${job.trafficLight}</td>
                                    <td>${job.jobUUID}</td>
                                    <td>${job.executedBy}</td>
                                </tr>
                                </tbody>`;
                });
                
                const table: string = `${title}<table>${haeder}${tableContent}</table>`;
                return table;
            } else {
                return this.noJobsRunnedMessage;
            }
        }
        return this.failedToFetchJobsMessage;
    }

    private formatDate(dateString: string | undefined) {
        if (dateString === '' || dateString === undefined) {
            return 'No date provided';
        }
        const date = new Date(dateString);
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        const time = date.toTimeString().split(' ')[0];
        return `${day}.${month}.${year} ${time}`;
    }

}   