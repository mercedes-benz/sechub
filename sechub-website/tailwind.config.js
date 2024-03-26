/** @type {import('tailwindcss').Config} */
export default {
  content: [],
  theme: {
    extend: {
      colors: {
        fern: {
          50: '#f5faf3',
          100: '#e7f4e4',
          200: '#d0e7cb',
          300: '#aad4a1',
          400: '#6daf5f',
          500: '#5a9b4c',
          600: '#467e3b',
          700: '#3a6431',
          800: '#30512a',
          900: '#294324',
          950: '#122310'
        }
      },
      animation: {
        'fade-in': 'fade-in 0.5s linear forwards',
        marquee: 'marquee var(--marquee-duration) linear infinite',
        'spin-slow': 'spin 4s linear infinite',
        'spin-slower': 'spin 6s linear infinite',
        'spin-reverse': 'spin-reverse 1s linear infinite',
        'spin-reverse-slow': 'spin-reverse 4s linear infinite',
        'spin-reverse-slower': 'spin-reverse 6s linear infinite'
      },
      keyframes: {
        'fade-in': {
          from: {
            opacity: '0'
          },
          to: {
            opacity: '1'
          }
        },
        marquee: {
          '100%': {
            transform: 'translateY(-50%)'
          }
        },
        'spin-reverse': {
          to: {
            transform: 'rotate(-360deg)'
          }
        }
      }
    }
  },
  plugins: []
};
