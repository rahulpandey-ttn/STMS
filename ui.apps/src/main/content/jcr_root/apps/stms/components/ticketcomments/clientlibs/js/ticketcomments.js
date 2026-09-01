(function () {
    'use strict';

    var TEXT_MAX_LENGTH = 5000;

    function setCsrfToken(form) {
        var tokenInput = form.querySelector('[name=":cq_csrf_token"]');
        if (!tokenInput || tokenInput.value) {
            return Promise.resolve();
        }

        return fetch('/libs/granite/csrf/token.json', { credentials: 'same-origin' })
            .then(function (response) {
                return response.json();
            })
            .then(function (data) {
                if (data && data.token) {
                    tokenInput.value = data.token;
                }
            })
            .catch(function () {
                // CSRF endpoint unavailable; allow AEM filter to handle rejection.
            });
    }

    function getFieldErrorElement(field) {
        var errorId = field.getAttribute('aria-describedby');
        return errorId ? document.getElementById(errorId) : null;
    }

    function setFieldError(field, message) {
        var errorElement = getFieldErrorElement(field);
        if (message) {
            field.classList.add('stms-field__input--invalid');
            field.setAttribute('aria-invalid', 'true');
            if (errorElement) {
                errorElement.textContent = message;
                errorElement.hidden = false;
            }
            return;
        }

        field.classList.remove('stms-field__input--invalid');
        field.removeAttribute('aria-invalid');
        if (errorElement) {
            errorElement.textContent = '';
            errorElement.hidden = true;
        }
    }

    function validateText(field) {
        var value = field.value.trim();
        if (!value) {
            return 'Comment is required.';
        }
        if (value.length > TEXT_MAX_LENGTH) {
            return 'Comment must be ' + TEXT_MAX_LENGTH + ' characters or fewer.';
        }
        return '';
    }

    function validateForm(form) {
        var textField = form.querySelector('[name="text"]');
        if (!textField) {
            return true;
        }

        var message = validateText(textField);
        setFieldError(textField, message);
        if (message) {
            textField.focus();
            return false;
        }

        return true;
    }

    function bindFieldValidation(form) {
        var textField = form.querySelector('[name="text"]');
        if (!textField) {
            return;
        }

        textField.addEventListener('blur', function () {
            setFieldError(textField, validateText(textField));
        });

        textField.addEventListener('input', function () {
            if (textField.getAttribute('aria-invalid') === 'true') {
                setFieldError(textField, validateText(textField));
            }
        });
    }

    function submitForm(form) {
        setCsrfToken(form).then(function () {
            form.submit();
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        var forms = document.querySelectorAll('.cmp-ticketcomments__form');
        forms.forEach(function (form) {
            setCsrfToken(form);
            bindFieldValidation(form);

            form.addEventListener('submit', function (event) {
                event.preventDefault();

                if (!validateForm(form)) {
                    return;
                }

                submitForm(form);
            });
        });
    });
}());
