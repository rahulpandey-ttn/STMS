(function () {
    'use strict';

    var TITLE_MAX_LENGTH = 200;
    var EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

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

    function getValidPriorities(form) {
        var priorityField = form.querySelector('[name="priority"]');
        if (!priorityField) {
            return [];
        }

        return Array.prototype.map.call(priorityField.options, function (option) {
            return option.value;
        }).filter(function (value) {
            return value;
        });
    }

    function validateTitle(field) {
        var value = field.value.trim();
        if (!value) {
            return 'Title is required.';
        }
        if (value.length > TITLE_MAX_LENGTH) {
            return 'Title must be ' + TITLE_MAX_LENGTH + ' characters or fewer.';
        }
        return '';
    }

    function validateDescription(field) {
        if (!field.value.trim()) {
            return 'Description is required.';
        }
        return '';
    }

    function validatePriority(field, validPriorities) {
        if (!field.value || validPriorities.indexOf(field.value) === -1) {
            return 'A valid priority is required.';
        }
        return '';
    }

    function validateAssignee(field) {
        var value = field.value.trim();
        if (!value) {
            return '';
        }
        if (!EMAIL_PATTERN.test(value)) {
            return 'Enter a valid email address for the assignee.';
        }
        return '';
    }

    function validateForm(form) {
        var validPriorities = getValidPriorities(form);
        var fields = [
            { field: form.querySelector('[name="title"]'), validate: validateTitle },
            { field: form.querySelector('[name="description"]'), validate: validateDescription },
            {
                field: form.querySelector('[name="priority"]'),
                validate: function (priorityField) {
                    return validatePriority(priorityField, validPriorities);
                }
            },
            { field: form.querySelector('[name="assignee"]'), validate: validateAssignee }
        ];

        var firstInvalidField = null;
        var isValid = true;

        fields.forEach(function (entry) {
            if (!entry.field) {
                return;
            }

            var message = entry.validate(entry.field);
            setFieldError(entry.field, message);
            if (message) {
                isValid = false;
                if (!firstInvalidField) {
                    firstInvalidField = entry.field;
                }
            }
        });

        if (firstInvalidField) {
            firstInvalidField.focus();
        }

        return isValid;
    }

    function bindFieldValidation(form) {
        var fields = form.querySelectorAll('[name="title"], [name="description"], [name="priority"], [name="assignee"]');
        var validPriorities = getValidPriorities(form);

        fields.forEach(function (field) {
            field.addEventListener('blur', function () {
                if (field.name === 'title') {
                    setFieldError(field, validateTitle(field));
                } else if (field.name === 'description') {
                    setFieldError(field, validateDescription(field));
                } else if (field.name === 'priority') {
                    setFieldError(field, validatePriority(field, validPriorities));
                } else if (field.name === 'assignee') {
                    setFieldError(field, validateAssignee(field));
                }
            });

            field.addEventListener('input', function () {
                if (field.getAttribute('aria-invalid') === 'true') {
                    if (field.name === 'title') {
                        setFieldError(field, validateTitle(field));
                    } else if (field.name === 'description') {
                        setFieldError(field, validateDescription(field));
                    } else if (field.name === 'priority') {
                        setFieldError(field, validatePriority(field, validPriorities));
                    } else if (field.name === 'assignee') {
                        setFieldError(field, validateAssignee(field));
                    }
                }
            });
        });
    }

    function submitForm(form) {
        setCsrfToken(form).then(function () {
            form.submit();
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        var forms = document.querySelectorAll('.cmp-ticketcreate__form');
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
