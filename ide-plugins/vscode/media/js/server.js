/* eslint-disable prettier/prettier */
/* eslint-disable no-undef */

const vscode = acquireVsCodeApi();
/* JavaScript for SecHub Server Webview */

/* --- Server State Container --- */
const setupServerStateContainer = () => {
  document.getElementById('serverUrlContainer').addEventListener('click', () => {
    vscode.postMessage({ type: 'changeServerUrl' });
  });

  document.getElementById('serverUserContainer').addEventListener('click', () => {
    vscode.postMessage({ type: 'changeCredentials' });
  });

  const openWebUiBtn = document.getElementById('openWebUiBtn');
  openWebUiBtn.addEventListener('click', () => {
    vscode.postMessage({ type: 'openWebUi', data: { leftClick: true } });
  });

  openWebUiBtn.addEventListener('contextmenu', (event) => {
    event.preventDefault();
    vscode.postMessage({ type: 'openWebUi', data: { leftClick: false } });
  });
};

/* --- Job Table --- */
const setupJobTable = () => {
  const changeProjectBtn = document.getElementById('changeProjectBtn');
  if (changeProjectBtn) {
    changeProjectBtn.addEventListener('click', () => {
      vscode.postMessage({ type: 'changeProject' });
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    const jobRows = document.querySelectorAll('.sechub-job-row');
    jobRows.forEach(row => {
      row.addEventListener('click', () => {
        // Remove 'selected' class from all rows
        jobRows.forEach(otherRow => otherRow.classList.remove('selected'));
        // Add 'selected' class to the clicked row
        row.classList.add('selected');

        const jobUUID = row.getAttribute('data-job-uuid');
        const projectId = row.getAttribute('data-project-id');
        const result = row.getAttribute('data-result');
        vscode.postMessage({ type: 'fetchReport', jobUUID, projectId, result });
      });
    });
  });
};

/* --- Pagination --- */
const setupPagination = () => {
  const prevPageBtn = document.getElementById('prevPageBtn');
  const nextPageBtn = document.getElementById('nextPageBtn');

  if (prevPageBtn) {
    prevPageBtn.addEventListener('click', () => {
      vscode.postMessage({ type: 'changePage', direction: 'prev' });
    });
  }
  if (nextPageBtn) {
    nextPageBtn.addEventListener('click', () => {
      vscode.postMessage({ type: 'changePage', direction: 'next' });
    });
  }
};

/* --- Initialize --- */
setupServerStateContainer();
setupJobTable();
setupPagination();