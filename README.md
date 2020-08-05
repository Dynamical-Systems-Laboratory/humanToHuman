# HumanToHuman
HumanToHuman is an Android and iOS app built to track the movement of people, for
use in academic research.

## Developing
Read `ios/CODE_ORGANIZATION.md` or `android/CODE_ORGANIZATION.md` for more information
about the mobile apps. Read below for more information about the server/data analysis.

## Code Organization
The code is laid out as follows:

```
.
├── analysis
│  ├── Analysis_simple.py
│  ├── boxplots.py
│  ├── error.py
│  ├── LIU_Analysis.ipynb
│  ├── LIU_Analysis.py
│  ├── rssi_connections.py
│  └── tompkin-2.py
├── database
│  ├── database.go
│  ├── migrate.go
│  └── models.go
├── go.mod
├── go.sum
├── humanToHuman
├── humanToHuman.exe
├── LICENSE
├── main.go
├── migrations
│  ├── 1_initial_schema.down.sql
│  └── 1_initial_schema.up.sql
├── pkged.go
├── README.md
├── templates
│  └── index.html
├── test
│  └── utils.go
├── utils
│  ├── argparse.go
│  ├── errors.go
│  ├── http.go
│  └── security.go
└── web
```


- `analysis` - The data processing used to make graphs out of the existing data
- `database` - Code that interacts with the PostGres database
- `migrations` - Code that runs to setup and tear down the database
- `templates` - HTML that is displayed to the researcher
- `utils` - Various utilities

To build the project, use `go build` in the project root; note that if you modify
the migrations or templates, you'll need to first regenerate the `pkged.go` file
using [pkger](https://github.com/markbates/pkger).


