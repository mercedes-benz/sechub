// SPDX-License-Identifier: MIT
import { ProjectData, UserListsJobsForProjectRequest } from 'sechub-openapi-ts-client';
import { DefaultClient } from '../api/defaultClient';
import { SECHUB_CONTEXT_STORAGE_KEYS } from '../utils/sechubConstants';
import { Pagination } from './pagination';
import * as vscode from 'vscode';

export class JobListTable {

    private pagination: Pagination = new Pagination(1, 0, 10, '');

    private noJobsRanMessage = '<p>No jobs have been started for your project.</p>';
    private failedToFetchJobsMessage = '<p>Failed to retrieve job list. You are either not allowed to view them or facing server connection issues.</p>';
    private noProjectSelectedMessage = '<p>Please select a project first.</p>';
    
    async renderJobTable(context: vscode.ExtensionContext) : Promise<string> {

        const project: ProjectData | undefined = context.globalState.get(SECHUB_CONTEXT_STORAGE_KEYS.selectedProject);
        const projectId = project?.projectId || 'No Project Selected';
        if (projectId !== this.pagination.getCurrentProject()){
            this.resetPagination();
            this.pagination.setCurrentProject(projectId);
        }
        
        const title = `<div id="jobTableTitleContainer">
                <p id="sechubJobTableTitle">Project 
                <span>${projectId}</span>
                </p>
                <button id="changeProjectBtn" class="sechubButton">Change Project</button>
        </div>`;

        if (!project) {
            return `${title}${this.noProjectSelectedMessage}`;
        }

        const client = await DefaultClient.getInstance(context);
            const requestParameter: UserListsJobsForProjectRequest = {
            projectId: projectId,
            size: this.pagination.getPageSize().toString(),
            page: (this.pagination.getCurrentPage() - 1).toString() // API expects zero-based page index
        };
        const data = await client.userListsJobsForProject(projectId, requestParameter);
        if(data){
            this.pagination.setTotalPages(data.totalPages || 0);
            if(data.content && data.content.length > 0) {
                const haeder  = `
                <thead>
                    <tr>
                        <th>Created</th>
                        <th>Status</th>
                        <th>Result</th>
                        <th></th>
                        <th>Job UUID</th>
                        <th>Executed By</th>
                    </tr>
                    <thead>`;

                let tableContent = '';
                data.content.forEach(job => {
                    const date = this.formatDate(job.created + '');
                    tableContent += `<tbody>
                                    <tr class="sechub-job-row" data-job-uuid="${job.jobUUID}" data-project-id="${projectId}">
                                    <td>${date}</td>
                                    <td>${job.executionState}</td>
                                    <td>${job.executionResult}</td>
                                    <td id="sechubJobTableTrafficLight"><span class="traffic-light ${job.trafficLight?.toLowerCase()}"></span></td>
                                    <td>${job.jobUUID}</td>
                                    <td>${job.executedBy}</td>
                                </tr>
                                </tbody>`;
                });
                
                const table: string = `${title}<table id="sechubJobTable" class="sechubTable">${haeder}${tableContent}</table>`;
                const paginationControls = this.pagination.renderPaginationControls();

                return `${table}${paginationControls}`;
            } else {
                return `${title}${this.noJobsRanMessage}`;
            }
        }
        return `${title}${this.failedToFetchJobsMessage}`;
    }

    changePage(direction: string) {
        if (direction === 'next') {
            if (this.pagination.getCurrentPage() < this.pagination.getTotalPages()) {
                this.pagination.setCurrentPage(this.pagination.getCurrentPage() + 1);
            }
        } else if (direction === 'prev') {
            if (this.pagination.getCurrentPage() > 1) {
                this.pagination.setCurrentPage(this.pagination.getCurrentPage() - 1);
            }
        }
    }

    resetPagination() {
        this.pagination = new Pagination(1, 0, 10);
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