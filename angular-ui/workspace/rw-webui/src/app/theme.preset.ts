import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';

/**
 * Surface values match PrimeNG's official "Soho" palette from the
 * showcase configurator (not a built-in @primeuix/themes primitive).
 */
export const AppTheme = definePreset(Aura, {
  primitive: {
    soho: {
      0: '#ffffff',
      50: '#ececec',
      100: '#dedfdf',
      200: '#c4c4c6',
      300: '#adaeb0',
      400: '#97979b',
      500: '#7f8084',
      600: '#6a6b70',
      700: '#55565b',
      800: '#3f4046',
      900: '#2c2c34',
      950: '#16161d',
    },
  },
  semantic: {
    primary: {
      50: '{emerald.50}',
      100: '{emerald.100}',
      200: '{emerald.200}',
      300: '{emerald.300}',
      400: '{emerald.400}',
      500: '{emerald.500}',
      600: '{emerald.600}',
      700: '{emerald.700}',
      800: '{emerald.800}',
      900: '{emerald.900}',
      950: '{emerald.950}',
    },
    colorScheme: {
      light: {
        surface: {
          0: '{soho.0}',
          50: '{soho.50}',
          100: '{soho.100}',
          200: '{soho.200}',
          300: '{soho.300}',
          400: '{soho.400}',
          500: '{soho.500}',
          600: '{soho.600}',
          700: '{soho.700}',
          800: '{soho.800}',
          900: '{soho.900}',
          950: '{soho.950}',
        },
      },
      dark: {
        surface: {
          0: '{soho.0}',
          50: '{soho.50}',
          100: '{soho.100}',
          200: '{soho.200}',
          300: '{soho.300}',
          400: '{soho.400}',
          500: '{soho.500}',
          600: '{soho.600}',
          700: '{soho.700}',
          800: '{soho.800}',
          900: '{soho.900}',
          950: '{soho.950}',
        },
      },
    },
  },
});
