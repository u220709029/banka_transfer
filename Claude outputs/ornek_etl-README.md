# Bank ETL Pipeline

A small Python ETL (Extract, Transform, Load) project. It reads credit card transactions from a CSV file, removes bad rows, and saves the clean data into a SQLite database. If something goes wrong, it writes a log and sends an alert e-mail.

## What it does

1. **Extract** – reads `kredi_karti_islemleri.csv` (separator `;`, encoding `utf-8-sig`).
2. **Transform** – checks the data and drops every row that has no `Musteri_TC` (customer ID).
3. **Load** – appends the clean rows to the `kredi_karti_islemleri` table in `banka_veritabani.db` (SQLite).
4. **Log** – every step is written to `banka_etl_sureci.log`.
5. **Alert** – if the pipeline crashes, it sends an e-mail through Gmail SMTP with the error message.

The pipeline stops with an error when the file is missing, the data is empty, or the `Musteri_TC` column does not exist.

## Tech stack

- Python 3
- pandas
- SQLite (`sqlite3` from the standard library)
- python-dotenv
- `logging` and `smtplib` from the standard library

## Project structure

```
ornek_etl/
├── veri_temizleme.py          # the ETL pipeline (run this file)
├── kredi_karti_islemleri.csv  # sample input data
├── bos_veri.csv               # empty file with only the header (for testing the error case)
├── banka_veritabani.db        # SQLite database (output)
├── baslatici.bat              # Windows launcher script
└── main.py                    # default IDE template file, not used by the pipeline
```

## Input format

The CSV file uses `;` as separator:

```
Islem_ID;Musteri_TC;Tarih;Tutar;Durum
1001;12345678901;12-05-2023;₺1.500,50;Basarili
1002;;13-05-2023;₺200,00;Basarili
```

In this sample, row `1002` has no `Musteri_TC`, so it is removed.

## How to run

1. Install the packages:

   ```bash
   pip install pandas python-dotenv
   ```

2. (Optional) Create a `.env` file next to the script if you want e-mail alerts:

   ```
   GONDERICI_EPOSTA=your_gmail_address@gmail.com
   GMAIL_UYGULAMA_SIFRESI=your_gmail_app_password
   ALICI_EPOSTA=where_to_send_alerts@example.com
   ```

   Use a Gmail **app password**, not your normal password. Without `GMAIL_UYGULAMA_SIFRESI`, the pipeline still runs. It only writes a warning to the log instead of sending an e-mail.

3. Run the pipeline:

   ```bash
   python veri_temizleme.py
   ```

   On Windows you can also use `baslatici.bat`. Edit the paths inside it first, because they point to a local folder.

## Settings

All settings are in the `PipelineConfig` class in `veri_temizleme.py`: CSV path, database path, table name, log file name, and SMTP server.

## Notes

- Every run **appends** to the table. Running the same file twice will create duplicate rows.
- Only missing `Musteri_TC` values are cleaned for now. Dates (`Tarih`) and amounts (`Tutar`, for example `₺1.500,50`) are saved as they are.

## Ideas for the future

- Convert dates and amounts to real date and number types.
- Avoid duplicate rows when the same file is loaded again.
- Add tests.
