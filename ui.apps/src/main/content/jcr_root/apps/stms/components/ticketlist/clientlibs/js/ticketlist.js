(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        var panels = document.querySelectorAll('.cmp-ticketlist__filters-panel');
        panels.forEach(function (panel) {
            var toggle = panel.querySelector('.cmp-ticketlist__filters-toggle');
            if (!toggle) {
                return;
            }

            toggle.addEventListener('click', function () {
                var collapsed = panel.classList.toggle('stms-panel--collapsed');
                toggle.setAttribute('aria-expanded', collapsed ? 'false' : 'true');
            });
        });

        var forms = document.querySelectorAll('.cmp-ticketlist__filters');
        forms.forEach(function (form) {
            form.addEventListener('submit', function () {
                var inputs = form.querySelectorAll('input, select');
                inputs.forEach(function (input) {
                    if (input.value === '') {
                        input.disabled = true;
                    }
                });
            });
        });
    });
}());
