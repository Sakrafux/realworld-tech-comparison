# RealWorld Backend: Python + FastAPI ("Hive" - Vertical Hexagonal)

This is a conceptual layout for a **Vertical Hexagonal Architecture** (Hive) using FastAPI. It combines Pydantic's validation with a strict separation of business logic.

## Architectural Idea
Each directory in the `app/` folder represents a **Cell** (a mini-hexagon). We use Python's **Protocols** to define Ports, allowing for easy swapping of database implementations.

- **Pydantic as Boundaries**: Pydantic models define the "Contracts" for data entering and leaving the core.
- **Dependency Injection**: FastAPI's `Depends` system is used to inject Repository Adapters into Services, and Services into Routers.
- **Pure Core**: The `service.py` contains only business logic and is decoupled from the specific ORM or Web framework.

## Potential Directory Structure

```text
.
├── app/
│   ├── users/               # --- USER CELL ---
│   │   ├── domain.py        # Pydantic schemas (Business Models)
│   │   ├── ports.py         # Python Protocols (Interfaces)
│   │   ├── service.py       # Core logic implementation
│   │   ├── repository.py    # Persistence Adapter (SQLAlchemy/SQLModel)
│   │   └── router.py        # FastAPI endpoints
│   │
│   ├── articles/            # --- ARTICLE CELL ---
│   │   └── ...
│   │
│   ├── shared/              # Shared logic (Auth, Logger, Base Models)
│   └── main.py              # Application initialization and routing
├── requirements.txt
└── README.md
```

## Why this works for FastAPI
FastAPI is lightweight and unopinionated. Its built-in DI system and reliance on Python type hints make it extremely easy to define and satisfy **Ports**. Using **Protocols** allows for true Hexagonal decoupling without the heavy inheritance structures required in Java.
