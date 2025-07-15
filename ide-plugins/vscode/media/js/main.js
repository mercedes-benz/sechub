const vscode = acquireVsCodeApi();

document.getElementById('changeProjectBtn').addEventListener('click', () => {
  vscode.postMessage({ type: 'changeProject' });
});

document.getElementById('serverUrlContainer').addEventListener('click', () => {
  vscode.postMessage({ type: 'changeServerUrl' });
});

document.getElementById('serverUserContainer').addEventListener('click', () => {
  vscode.postMessage({ type: 'changeCredentials' });
});

document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.sechub-job-row').forEach(row => {
    row.addEventListener('click', () => {
      const jobUUID = row.getAttribute('data-job-uuid');
      const projectId = row.getAttribute('data-project-id');
        vscode.postMessage({ type: 'fetchReport', jobUUID, projectId });
    });
  });
});