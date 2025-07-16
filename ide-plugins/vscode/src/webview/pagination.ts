// Pagination.ts
export class Pagination {
    private currentProject: string | undefined;
    private currentPage: number;
    private totalPages: number;
    private pageSize: number;
    
    constructor(currentPage: number, totalPages: number, pageSize: number, currentProject?: string) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.currentProject = currentProject;
    }

    renderPaginationControls(): string {

        return `
        <div id="pagination" class="pagination-container">
            <button class="icon sechubButton" id="prevPageBtn" ${this.currentPage === 1 ? 'disabled' : ''}><i class="codicon codicon-arrow-left" href="#"></i></button>
            <span>Page ${this.currentPage} of ${this.totalPages}</span>
			<button class="icon sechubButton" id="nextPageBtn" ${this.currentPage === this.totalPages ? 'disabled' : ''}><i class="codicon codicon-arrow-right" href="#"></i></button>
        </div>`;
    }

    setCurrentPage(newPage: number) {
        if (newPage > 0 && newPage <= this.totalPages) {
            this.currentPage = newPage;
        }
    }

    setTotalPages(totalPages: number) {
        this.totalPages = totalPages;
    }

    setCurrentProject(project: string) {
        this.currentProject = project;
    }

    getCurrentPage(): number {
        return this.currentPage;
    }

    getTotalPages(): number {
        return this.totalPages;
    }

    getPageSize(): number {
        return this.pageSize;
    }

    getCurrentProject(): string | undefined {
        return this.currentProject;
    }
}