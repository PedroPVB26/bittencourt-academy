import { Service } from '@angular/core';

type Theme = 'light' | 'dark';

@Service()
export class ThemeService {
    private readonly storageKey = 'theme';

    init() {
        const savedTheme = localStorage.getItem(this.storageKey) as Theme | null;

        if (savedTheme) {
            this.setTheme(savedTheme);
            return;
        }

        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

        this.setTheme(
            prefersDark ? 'dark' : 'light'
        );
    }

    toggle() {
        const isDark =  document.body.classList.contains('dark-theme');
        this.setTheme(
            isDark ? 'light' : 'dark'
        );
    }

    private setTheme(theme: Theme) {
        document.body.classList.toggle(
            'dark-theme',
            theme === 'dark'
        );

        localStorage.setItem(this.storageKey,theme);
    }
}
