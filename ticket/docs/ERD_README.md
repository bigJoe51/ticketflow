# TicketFlow ERD Artifacts

Generated from entity classes in:
- `src/main/java/com/kilimo/ticket/model`

## Files
- `ERD.drawio` - Native draw.io file (open directly in diagrams.net)
- `ERD.dbml` - DBML format (works in dbdiagram.io and many ERD tools)
- `ERD.mmd` - Mermaid ER diagram format

## draw.io import options

### Option 1 (Recommended): Mermaid
1. Open draw.io.
2. Go to `Arrange` -> `Insert` -> `Advanced` -> `Mermaid`.
3. Paste contents of `ERD.mmd`.
4. Click `Insert`.

### Option 1b: Native draw.io
1. Open draw.io / diagrams.net.
2. `File` -> `Open From` -> `Device`.
3. Select `ERD.drawio`.

### Option 2: DBML
Use DBML tools (for example dbdiagram.io) to render/export, then import/export visuals as needed.

## Notes
- Cardinalities are mapped from JPA annotations (`@ManyToOne`, `@OneToMany`, `@OneToOne`).
- Table/column names follow `@Table` and `@JoinColumn` names from your models.
