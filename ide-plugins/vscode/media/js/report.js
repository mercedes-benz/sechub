const vscode = acquireVsCodeApi();

/* reportTable */
function toggleDiv(divId) {
    var div = document.getElementById(divId);
    if (div.style.display === "none") {
        div.style.display = "block";
    } else {
        div.style.display = "none";
    }
}

document.addEventListener('DOMContentLoaded', () => {
  // Expandable headers
  document.querySelectorAll('.expandable-header').forEach(header => {
    header.addEventListener('click', function() {
      const targetId = this.getAttribute('data-target');
      const targetDiv = document.getElementById(targetId);
      if (targetDiv.style.display === "none" || targetDiv.style.display === "") {
        targetDiv.style.display = "block";
      } else {
        targetDiv.style.display = "none";
      }
      header.classList.toggle('expanded');
    });
  });

  // Finding row clicks
  const findingRows = document.querySelectorAll('.sechub-finding-row');
  findingRows.forEach(row => {
    row.addEventListener('click', () => {

      findingRows.forEach(otherRow => {
        otherRow.classList.remove('selected');
      });

      row.classList.add('selected');

      const findingId = row.getAttribute('data-finding-id');
      vscode.postMessage({ type: 'openFinding', findingId });
    });
  });

  /* False Positives Checkbox Handling */
  const selectAllCheckbox = document.getElementById('selectAllFalsePositives');
  const tableBody = document.getElementById('sechubReportTable');
  const markAsFalsePositiveButton = document.getElementById('markAsFalsePositiveButton');
  const checkboxes = tableBody.querySelectorAll('.item-checkbox');
  if (checkboxes.length === 0) {
    markAsFalsePositiveButton.disabled = true;
    selectAllCheckbox.style.display = "none";
  }


  function updateButtonState() {
    const checkboxes = tableBody.querySelectorAll('.item-checkbox:checked');
    markAsFalsePositiveButton.disabled = checkboxes.length === 0;
    markAsFalsePositiveButton.style.display = checkboxes.length > 0 ? "inline-block" : "none";
  }

  updateButtonState();

  selectAllCheckbox.addEventListener('change', function() {
    let checkboxes = tableBody.querySelectorAll('.item-checkbox');

    checkboxes.forEach(checkbox => {
      checkbox.checked = selectAllCheckbox.checked;
      checkbox.disabled = selectAllCheckbox.checked ? checkbox.disabled : checkbox.disabled;
    });
    
    updateButtonState();
  
  });

  tableBody.addEventListener('change', function(event) {
    if (event.target.classList.contains('item-checkbox')) {
      updateSelectAllCheckbox();
      updateButtonState();
    }
  });

  markAsFalsePositiveButton.addEventListener('click', function() {
    const selectedCheckboxes = tableBody.querySelectorAll('.item-checkbox:checked');
    const findingIds = Array.from(selectedCheckboxes).map(checkbox => {
        const row = checkbox.closest('.sechub-finding-row');
        // ensure finding id is an integer
        return parseInt(row.getAttribute('data-finding-id'), 10);
    });

    vscode.postMessage({
      type: 'markAsFalsePositive',
      findingIds: findingIds
    });
  });

  function updateSelectAllCheckbox() {
    const checkboxes = tableBody.querySelectorAll('.item-checkbox');
    const allChecked = Array.from(checkboxes).every(checkbox => checkbox.checked);
    selectAllCheckbox.checked = allChecked;
  }


  // Open CWE in browser button
  const openCWEButtons = document.querySelectorAll('#openCWEinBrowserButton');
  openCWEButtons.forEach(button => {
    button.addEventListener('click', (event) => {
      event.stopPropagation(); // Prevent row click event
      const cweId = button.textContent.trim();
      if (cweId) {
        vscode.postMessage({ type: 'openCWEInBrowser', cweId });
      }
    });
  });
});