(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        var shells = document.querySelectorAll('.cmp-appshell');
        shells.forEach(function (shell) {
            var toggle = shell.querySelector('.cmp-appshell__menu-toggle');
            if (!toggle) {
                return;
            }

            toggle.addEventListener('click', function () {
                var open = shell.classList.toggle('stms-app--nav-open');
                toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
            });
        });
    });
}());
