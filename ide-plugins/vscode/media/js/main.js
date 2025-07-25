const vscode = acquireVsCodeApi();

/* serverStateContainer */
document.getElementById('serverUrlContainer').addEventListener('click', () => {
  vscode.postMessage({ type: 'changeServerUrl' });
});

document.getElementById('serverUserContainer').addEventListener('click', () => {
  vscode.postMessage({ type: 'changeCredentials' });
});

const openWebUiBtn = document.getElementById('openWebUiBtn')
openWebUiBtn.addEventListener('click', () => {
  vscode.postMessage({ type: 'openWebUi', data: { leftClick: true } });
});
openWebUiBtn.addEventListener('contextmenu', (event) => {
  event.preventDefault();
  vscode.postMessage({ type: 'openWebUi', data: { leftClick: false } });
});

/* jobTable */
const changeProjectBtn = document.getElementById('changeProjectBtn');
if (changeProjectBtn) {
  changeProjectBtn.addEventListener('click', () => {
    vscode.postMessage({ type: 'changeProject' });
  });
}

document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.sechub-job-row').forEach(row => {
    row.addEventListener('click', () => {
      const jobUUID = row.getAttribute('data-job-uuid');
      const projectId = row.getAttribute('data-project-id');
        vscode.postMessage({ type: 'fetchReport', jobUUID, projectId });
    });
  });
});

/* pagination */
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