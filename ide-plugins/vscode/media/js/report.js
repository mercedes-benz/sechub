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
  document.querySelectorAll('.sechub-finding-row').forEach(row => {
    row.addEventListener('click', () => {
      const findingId = row.getAttribute('data-finding-id');
      vscode.postMessage({ type: 'openFinding', findingId });
    });
  });

  /* False Positives Checkbox Handling */
  const selectAllCheckbox = document.getElementById('selectAllFalsePositives');
  const tableBody = document.getElementById('sechubReportTable');
  const markAsFalsePositiveButton = document.getElementById('markAsFalsePositiveButton');

  function updateButtonState() {
    const checkboxes = tableBody.querySelectorAll('.item-checkbox:checked');
    markAsFalsePositiveButton.disabled = checkboxes.length === 0;
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
      console.log('Checkbox change event fired!'); // Debugging
      updateSelectAllCheckbox();
      updateButtonState();
    }
  });

  markAsFalsePositiveButton.addEventListener('click', function() {
    const selectedCheckboxes = tableBody.querySelectorAll('.item-checkbox:checked');
    const findingIds = Array.from(selectedCheckboxes).map(checkbox => {
        const row = checkbox.closest('.sechub-finding-row');
        return row.getAttribute('data-finding-id');
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
});