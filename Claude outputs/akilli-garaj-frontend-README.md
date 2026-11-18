# Akıllı Garaj – Frontend

The web interface of the Smart Garage app. You can add your vehicles, enter fuel purchases and maintenance records, and see simple reports like average fuel consumption and cost per kilometer.

This app needs the backend to work: [akilliGaraj](https://github.com/u220709029/akilliGaraj)

<!-- Add a screenshot here, for example: ![Dashboard](docs/dashboard.png) -->

## Pages

| Route | Page | What you can do |
|-------|------|-----------------|
| `/` | Garage dashboard | See all vehicles, add a fuel entry, add a maintenance record, open the maintenance history |
| `/add-vehicle` | Add vehicle | Enter license plate, brand, model, production year, and odometer |
| `/vehicle/:id` | Fuel analysis report | See average consumption, cost per km, total distance, and total spent |

## Tech stack

- React 19
- Vite
- React Router
- Tailwind CSS 4
- Axios
- ESLint

## Getting started

### Requirements

- Node.js 20.19 or newer
- The backend running at `http://localhost:8080`

### Steps

```bash
npm install
npm run dev
```

Open http://localhost:5173. The backend allows requests from this address.

### Other commands

| Command | Description |
|---------|-------------|
| `npm run build` | Build the app for production |
| `npm run preview` | Preview the production build |
| `npm run lint` | Check the code with ESLint |

## Project structure

```
src/
├── main.jsx            # app entry point
├── App.jsx             # routes
└── pages/
    ├── Dashboard.jsx     # vehicle list, fuel and maintenance forms
    ├── AddVehicle.jsx    # add vehicle form
    └── VehicleDetail.jsx # fuel analysis report
```

## Notes

- The backend address (`http://localhost:8080`) is written directly in the page files.
- The app currently works with user ID `1`. There is no login screen yet.

## Roadmap

- Login page
- Move the API address to an environment variable
- Charts for fuel and cost history
