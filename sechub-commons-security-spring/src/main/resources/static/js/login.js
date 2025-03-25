function switchTab(tab) {
    const tabs = document.querySelectorAll('.tab');
    const loginOptions = document.querySelectorAll('.login-option');
    tabs.forEach(t => t.classList.remove('active'));
    loginOptions.forEach(option => option.classList.remove('active'));

    tab.classList.add('active');
    const index = Array.from(tabs).indexOf(tab);
    loginOptions[index].classList.add('active');
}

function openModal(id) {
    document.getElementById(id).style.display = 'block';
}

function closeModal(id) {
    document.getElementById(id).style.display = 'none';
}

function showAlert(message) {
    document.getElementById('alertMessage').textContent = message;
    openModal('alertModal');
}

async function handleFormSubmit(event) {
    event.preventDefault(); // Prevent the default form submission

    const emailInput = document.getElementById('email');
    const email = emailInput.value.trim();

    if (!email) {
        showAlert("Please enter a valid email address.");
        return;
    }

    await submitEmail(email);
}

async function submitEmail(email) {

    let message = '';
    try {
        const response = await fetch(`/api/anonymous/refresh/apitoken/${email}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        if (response.ok) {
            message = 'You will receive an email soon, follow the instructions in the email to create a new API token.';
        } else {
            message = 'An error occurred. Please try again.';
        }
    } catch (error) {
        console.error('Error:', error);
        message = 'An error occurred. Please try again.';
    }
    closeModal('forgetApiTokenModal');
    showAlert(message);
}