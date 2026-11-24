from __future__ import annotations

from datetime import date
from pathlib import Path
import re
import unicodedata

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
from docx.shared import Inches, Pt, RGBColor


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "docs"
DOCX_PATH = OUTPUT / "PrintShop_Customer_Manager_Project_Report.docx"
PDF_PATH = OUTPUT / "PrintShop_Customer_Manager_Project_Report.pdf"
REPORT_DATE = "24 November 2026"


SECTIONS = [
    (
        "3. Acknowledgement",
        [
            "I would like to express my gratitude to my project guide, faculty, institution, and everyone who supported the planning and development of this project. Their feedback helped shape the requirements and the application design.",
            "Student: [Student Name]    Project Guide: [Guide Name]",
        ],
    ),
    (
        "4. Abstract",
        [
            "PrintShop Customer Manager is a native Android application intended to help a printing shop maintain an organized customer directory. It is implemented in Kotlin with Jetpack Compose and Material 3, following an MVVM-oriented structure. Firebase Authentication provides email/password staff sign-in, and Cloud Firestore stores customer records.",
            "The application supports registration, live customer listing, search by name/phone/email, profile details, editing, deletion with confirmation, password reset, and a repeat-safe action to add ten synthetic demonstration customers. Form validation covers required fields, email, Indian mobile number, address length, valid past dates, and leap years. Firestore rules require authentication and validate document structure.",
            "Firebase credentials and an Android emulator were not available during report generation. Consequently, cloud integration and device screenshots are documented as setup/verification tasks rather than claimed as tested results.",
        ],
    ),
    (
        "5. Introduction",
        [
            "Printing shops interact with customers over recurring orders, quotations, artwork reviews, and delivery arrangements. Customer contact information kept in notebooks or scattered spreadsheets is difficult to search, update, and back up.",
            "This project provides a small mobile customer-management tool. A staff member authenticates, records a customer, searches and reviews the directory, and corrects or removes outdated entries. Firebase provides a managed identity service and cloud database, while the Android client provides the shop-floor interface.",
        ],
    ),
    (
        "6. Problem Statement",
        [
            "Manual customer records can be duplicated, become outdated, and take time to search. They can also be exposed when access is not controlled. The project addresses these operational problems by providing a searchable digital directory with authenticated cloud access and validated forms.",
        ],
    ),
    (
        "7. Objectives",
        [
            "• Develop a native Kotlin Android application using Jetpack Compose and Material 3.",
            "• Implement a maintainable separation between UI, screen/navigation, ViewModel, model, and Firebase repository layers.",
            "• Provide staff account registration, sign-in, sign-out, and password-reset flows.",
            "• Store customer profiles in Firestore with live list updates and required authentication.",
            "• Support validated create, search, read, edit, and confirmed-delete workflows.",
            "• Include repeat-safe synthetic seed records, automated validation tests, security rules, setup instructions, and an academic report.",
        ],
    ),
    (
        "8. Scope of the Project",
        [
            "The application manages customer contact/profile data only. It does not manage print-job estimates, artwork files, inventory, billing, payments, marketing, or multiple shop branches. It assumes staff accounts have already been provisioned and are trusted to access the customer collection.",
            "The Android application is a Firebase client. A Firebase project, Authentication provider, Firestore database, deployed rules, and app-specific google-services.json must be supplied by the deployment owner.",
        ],
    ),
    (
        "9. Existing System",
        [
            "A typical small-shop process records names and contact information in paper registers or personal spreadsheets. Such records are often device- or person-specific, offer limited validation, and do not provide consistent access control, live synchronization, or reliable record-change workflows.",
        ],
    ),
    (
        "10. Proposed System",
        [
            "The proposed system is an Android client backed by Firebase Authentication and Cloud Firestore. Authenticated staff can create, browse, search, inspect, edit, and delete records. Input checks prevent common mistakes before writes. A Firestore snapshot listener keeps the directory and dashboard count current while the app is open.",
            "The implementation is intentionally a small single-module app. MVVM separates observable UI state and actions from screens, and the repository isolates Firebase operations from the Compose UI.",
        ],
    ),
    (
        "11. Software and Hardware Requirements",
        [
            "Development machine: Windows, macOS, or Linux; 8 GB RAM recommended for Android Studio and an emulator.",
            "Development software: Android Studio, Java 17, Android SDK Platform 35, Gradle compatible with Android Gradle Plugin 8.7.2, and Firebase CLI for rules deployment.",
            "Run target: Android API 26 or newer emulator/device, network access, and a configured Firebase project.",
            "Services: Firebase Authentication with Email/Password enabled and Cloud Firestore with the included rules deployed.",
        ],
    ),
    (
        "12. Technologies Used",
        [
            "Kotlin 2.0.21; Android Gradle Plugin 8.7.2; Jetpack Compose and Material 3; Navigation Compose; AndroidX lifecycle ViewModel; Firebase Authentication; Cloud Firestore; Kotlin coroutines and Google Play Services Task await integration; JUnit 4 for local unit tests.",
        ],
    ),
    (
        "13. System Architecture",
        [
            "The single-activity Compose UI observes a StateFlow from MainViewModel. The ViewModel performs user actions in a coroutine and calls FirebaseRepository. FirebaseProvider exposes the configured Firebase Auth and Firestore instances. Customer is the application model. Navigation Compose owns the login, sign-up, dashboard, list, add, detail, and edit destinations.",
            "Architecture diagram (logical flow):",
            "Staff -> Compose screens -> Navigation Compose",
            "                         | state/actions",
            "                         v",
            "                    MainViewModel",
            "                         | suspend repository operations",
            "                         v",
            "                  FirebaseRepository",
            "                    /           \\",
            "          Firebase Auth       Cloud Firestore",
            "                                   |",
            "                          customers collection",
        ],
    ),
    (
        "14. Application Flowchart",
        [
            "                         +----------------+",
            "                         |   Launch app   |",
            "                         +-------+--------+",
            "                                 |",
            "                         +-------v--------+",
            "                         | Auth session?  |",
            "                         +---+--------+---+",
            "                             |        |",
            "                           No|        |Yes",
            "               +-------------v--+  +--v-----------+",
            "               | Login / Sign-up|  |  Dashboard   |",
            "               +------+---------+  +--+-----------+",
            "                      |               |",
            "            +---------v------+    +---v----------------+",
            "            | Firebase Auth  |    | Add / View records |",
            "            +----------------+    +--+--------------+--+",
            "                                     |              |",
            "                                +----v----+   +-----v------+",
            "                                | Validate|   | Search and |",
            "                                | and save|   | inspect    |",
            "                                +----+----+   +-----+------+",
            "                                     |              |",
            "                                     +------v-------+",
            "                                     |  Firestore   |",
            "                                     +--------------+",
        ],
    ),
    (
        "15. Use Case Diagram",
        [
            "Actor: Authenticated shop staff",
            "",
            "                    +----------------------------------------------+",
            "                    |       PrintShop Customer Manager             |",
            "Staff ------------>| Sign in / create staff account                |",
            "Staff ------------>| Reset password / sign out                    |",
            "Staff ------------>| View dashboard and customer count             |",
            "Staff ------------>| Add customer / validate profile               |",
            "Staff ------------>| Search and view customer details              |",
            "Staff ------------>| Edit customer                                 |",
            "Staff ------------>| Confirm and delete customer                   |",
            "Staff ------------>| Seed fictional demo customers                 |",
            "                    +----------------------------------------------+",
            "",
            "All customer-data use cases require a Firebase-authenticated session. The current rules treat all authenticated accounts as having the same customer-data permissions.",
        ],
    ),
    (
        "16. Database Design",
        [
            "Firestore uses a customers collection and one document per customer. Document IDs are auto-generated for normal records; the demo seeder uses stable IDs so repeated seeding does not duplicate its records.",
        ],
    ),
    (
        "17. Firebase Integration",
        [
            "Firebase Authentication handles email/password registration, login, reset email, and session persistence. An auth-state listener drives the application route. Cloud Firestore stores customer documents and publishes updates through a snapshot listener ordered by createdAt descending.",
            "For a new environment, create a Firebase project, register package com.printcare.app, add app/google-services.json locally, enable Email/Password, create Firestore, and deploy firestore.rules with the Firebase CLI. The Google Services Gradle plugin is applied only when the configuration file is present. No Firebase project credentials are included in this deliverable.",
            "Older PrintCare records with name/phone/dob fields are read compatibly. Editing an older record replaces it with the current schema and preserves its createdAt timestamp. Documents without a timestamp cannot be edited until they are repaired.",
        ],
    ),
    (
        "18. Screen-by-Screen Description",
        [
            "Login: email, password with visibility toggle, create-account navigation, password-reset dialog, validation, and Firebase setup notice. [Screenshot placeholder: capture Login on emulator.]",
            "Sign-up: staff name, email, password, confirmation, validation, and account creation. [Screenshot placeholder: capture Sign-up on emulator.]",
            "Dashboard: staff greeting, live customer count, add/view shortcuts, demo-seed action, and sign-out confirmation. [Screenshot placeholder: capture Dashboard on emulator.]",
            "Customer list: live customer cards, search by name/phone/email, loading, empty, and no-results states. [Screenshot placeholder: capture Customer list on emulator.]",
            "Add/Edit customer: name, phone, date picker, email, address, field errors, save progress, and cancel. [Screenshot placeholder: capture Customer form on emulator.]",
            "Customer details: identifier, phone, DOB, email, address, registration timestamp, edit, and confirmed delete. [Screenshot placeholder: capture Customer details on emulator.]",
            "Actual emulator screenshots were not available while this report was generated because Android SDK/emulator and Firebase configuration were not present.",
        ],
    ),
    (
        "19. Testing and Test Cases",
        [
            "Local unit tests are included for validation helpers. End-to-end cases require Android Studio/SDK, a configured Firebase project, and an emulator/device. No integration test is claimed as executed.",
        ],
    ),
    (
        "20. Results",
        [
            "Implemented source includes the Compose screens and navigation, MVVM state/actions, Firebase Auth/Firestore repository, customer CRUD and live search, synthetic demo seeding, validation unit tests, deployment rules, setup README, and this report.",
            "Verification limitation: this environment did not contain Gradle, Android SDK, an emulator, or google-services.json. Therefore, Gradle compilation, unit-test execution, Firebase network operations, APK installation, and screenshots remain unverified. Run the checklist in README.md before academic submission or production use.",
        ],
    ),
    (
        "21. Limitations",
        [
            "All authenticated accounts share customer read/write/delete permissions; public sign-up makes this too permissive for production. Firebase setup and seed writes require connectivity. Client date validation cannot be treated as a security control, and Firestore rules only validate date-string shape. No offline conflict-resolution workflow, audit log, staff roles, print-job data, or actual emulator screenshot is included. This environment could not run a build or integration test.",
        ],
    ),
    (
        "22. Future Enhancements",
        [
            "Add trusted staff onboarding and role-based custom claims, per-shop tenant isolation, audit history, phone/email verification, profile import/export, print-order and quotation workflows, pagination, offline synchronization UX, stronger server-side date representation, automated Firestore emulator rule tests, CI build/test jobs, and automated emulator screenshot capture.",
        ],
    ),
    (
        "23. Conclusion",
        [
            "PrintShop Customer Manager provides a structured foundation for managing a printing shop's customer directory. Its Compose UI, observable ViewModel state, Firebase repository, input validation, and authenticated Firestore rules implement the requested core workflows. A Firebase owner must configure credentials and access policies and run the Android build and device checks before the application can be considered a verified deployment.",
        ],
    ),
    (
        "24. References",
        [
            "Android Developers, Jetpack Compose documentation: https://developer.android.com/develop/ui/compose",
            "Android Developers, Navigation Compose: https://developer.android.com/develop/ui/compose/navigation",
            "Android Developers, ViewModel overview: https://developer.android.com/topic/libraries/architecture/viewmodel",
            "Firebase documentation, Android setup: https://firebase.google.com/docs/android/setup",
            "Firebase documentation, Authentication: https://firebase.google.com/docs/auth/android/password-auth",
            "Firebase documentation, Cloud Firestore: https://firebase.google.com/docs/firestore",
            "Firebase documentation, Firestore security rules: https://firebase.google.com/docs/firestore/security/get-started",
        ],
    ),
]

TABLES = {
    "11. Software and Hardware Requirements": (
        ["Category", "Requirement"],
        [
            ["Development host", "Windows/macOS/Linux; 8 GB RAM recommended"],
            ["Toolchain", "Android Studio, Java 17, Android SDK Platform 35"],
            ["Target", "Android API 26+ emulator or device"],
            ["Backend", "Firebase Authentication and Cloud Firestore"],
        ],
    ),
    "16. Database Design": (
        ["Field", "Type", "Purpose"],
        [
            ["customerId", "String", "Firestore document ID"],
            ["fullName", "String", "Customer name"],
            ["phoneNumber", "String", "10-digit Indian mobile"],
            ["dateOfBirth", "String", "DD/MM/YYYY"],
            ["email", "String", "Customer email"],
            ["address", "String", "Postal/contact address"],
            ["createdAt", "Timestamp", "Server registration time"],
        ],
    ),
    "19. Testing and Test Cases": (
        ["Test case", "Expected result", "Status in this environment"],
        [
            ["Valid/invalid email, phone, name, address", "Correct validation result", "JUnit tests added; not run"],
            ["Leap day and impossible DOB", "Accept real date; reject impossible date", "JUnit tests added; not run"],
            ["Future DOB", "Reject future date", "JUnit test added; not run"],
            ["Login/register/password reset/logout", "Firebase Auth success and errors", "Not run; Firebase not configured"],
            ["Customer CRUD, search, seed", "Records sync and display correctly", "Not run; Firebase not configured"],
            ["Navigation/network failure", "Correct route and clear retryable error", "Not run on emulator"],
            ["Firestore rules", "Unauthenticated requests denied", "Rules supplied; emulator test not run"],
            ["Android build and APK", "Debug APK builds and installs", "Not run; Android SDK/Gradle unavailable"],
        ],
    ),
}


def add_table(document: Document, headers: list[str], rows: list[list[str]]) -> None:
    table = document.add_table(rows=1, cols=len(headers))
    table.style = "Table Grid"
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    for cell, value in zip(table.rows[0].cells, headers):
        cell.text = value
        cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
        for run in cell.paragraphs[0].runs:
            run.bold = True
    for row in rows:
        cells = table.add_row().cells
        for cell, value in zip(cells, row):
            cell.text = value
            cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
    document.add_paragraph()


def build_docx() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    document = Document()
    section = document.sections[0]
    section.top_margin = Inches(0.7)
    section.bottom_margin = Inches(0.7)
    section.left_margin = Inches(0.8)
    section.right_margin = Inches(0.8)

    normal = document.styles["Normal"]
    normal.font.name = "Aptos"
    normal.font.size = Pt(10)
    normal.font.color.rgb = RGBColor(35, 44, 58)
    document.styles["Title"].font.name = "Aptos Display"
    document.styles["Title"].font.size = Pt(28)
    document.styles["Title"].font.color.rgb = RGBColor(21, 94, 239)

    header = section.header.paragraphs[0]
    header.text = "PRINTSHOP CUSTOMER MANAGER  |  ACADEMIC PROJECT REPORT"
    header.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    for run in header.runs:
        run.font.size = Pt(8)
        run.font.color.rgb = RGBColor(100, 110, 125)
    footer = section.footer.paragraphs[0]
    footer.text = "PrintShop Customer Manager  •  [Institution Name]"
    footer.alignment = WD_ALIGN_PARAGRAPH.CENTER
    for run in footer.runs:
        run.font.size = Pt(8)

    title = document.add_paragraph()
    title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    title.add_run("\n\n\n")
    title.style = document.styles["Title"]
    title.add_run("PRINTSHOP\nCUSTOMER MANAGER")
    subtitle = document.add_paragraph()
    subtitle.alignment = WD_ALIGN_PARAGRAPH.CENTER
    subtitle.add_run("Printing Shop Customer Management Android Application").bold = True
    subtitle.add_run(f"\n\nAcademic Project Report\n\nSubmitted by\n[Student Name]\n[Roll Number]\n\nUnder the guidance of\n[Project Guide Name]\n\n[Department / Programme]\n[Institution Name]\n[City, Country]\n\n{REPORT_DATE}")
    document.add_page_break()

    document.add_heading("1. Cover Page", level=1)
    document.add_paragraph(
        "Project: PrintShop Customer Manager\n"
        "Programme / Department: [Department / Programme]\n"
        "Institution: [Institution Name]\n"
        "Submitted by: [Student Name]  |  Roll number: [Roll Number]\n"
        "Project guide: [Project Guide Name]\n"
        f"Submission date: {REPORT_DATE}"
    )
    document.add_heading("2. Certificate (Placeholder)", level=1)
    document.add_paragraph(
        "This is to certify that the project titled “PrintShop Customer Manager” was carried out by "
        "[Student Name], [Roll Number], under my guidance, in partial fulfilment of the requirements "
        "for [Degree / Programme] at [Institution Name]. The work and submission date are subject to "
        "institutional review.\n\n"
        "Project Guide: ____________________    Head of Department: ____________________\n\n"
        "Date: ____________________             Institution Seal: ____________________"
    )
    for heading, paragraphs in SECTIONS:
        document.add_heading(heading, level=1)
        table_spec = TABLES.get(heading)
        inserted_table = False
        for paragraph in paragraphs:
            if paragraph.startswith("Architecture diagram") or paragraph.startswith("                         +") or paragraph.startswith("                    +") or paragraph.startswith("Staff ----"):
                p = document.add_paragraph()
                run = p.add_run(paragraph)
                run.font.name = "Consolas"
                run.font.size = Pt(8)
            elif paragraph.startswith("• "):
                document.add_paragraph(paragraph[2:], style="List Bullet")
            elif paragraph == "":
                document.add_paragraph()
            else:
                document.add_paragraph(paragraph)
            if table_spec and heading == "16. Database Design" and paragraph.startswith("Collection:"):
                add_table(document, *table_spec)
                inserted_table = True
            if table_spec and heading == "19. Testing and Test Cases" and paragraph.startswith("Local unit"):
                add_table(document, *table_spec)
                inserted_table = True
        if table_spec and heading == "11. Software and Hardware Requirements":
            add_table(document, *table_spec)
        if table_spec and heading in {"16. Database Design", "19. Testing and Test Cases"} and not inserted_table:
            add_table(document, *table_spec)

    document.save(DOCX_PATH)


def ascii_text(value: str) -> str:
    value = value.replace("•", "-").replace("→", "->").replace("“", '"').replace("”", '"')
    return unicodedata.normalize("NFKD", value).encode("ascii", "ignore").decode("ascii")


def pdf_lines() -> list[str]:
    lines = [
        "PRINTSHOP CUSTOMER MANAGER",
        "Printing Shop Customer Management Android Application",
        "ACADEMIC PROJECT REPORT",
        f"Submitted by: [Student Name] | [Roll Number] | {REPORT_DATE}",
        "Institution: [Institution Name] | Project Guide: [Project Guide Name]",
        "",
        "1. Cover Page",
        "Project: PrintShop Customer Manager",
        "Programme: [Department / Programme] | Institution: [Institution Name]",
        "Student: [Student Name] | Roll number: [Roll Number]",
        f"Guide: [Project Guide Name] | Submission date: {REPORT_DATE}",
        "",
        "2. Certificate (Placeholder)",
        "This certifies that [Student Name], [Roll Number], completed the project titled PrintShop",
        "Customer Manager under the guidance of [Project Guide Name], subject to institutional review.",
        "Project Guide: ____________________  Head of Department: ____________________",
        "Date: ____________________           Institution Seal: ____________________",
        "",
    ]
    for heading, paragraphs in SECTIONS:
        lines.extend(["", heading, "-" * min(len(heading), 85)])
        for paragraph in paragraphs:
            raw = ascii_text(paragraph)
            if not raw:
                lines.append("")
                continue
            width = 90 if paragraph.startswith(("                    ", "                         ", "Staff ----")) else 96
            wrapped = re.findall(r".{1,%d}(?:\s+|$)" % width, raw) or [raw]
            lines.extend(item.rstrip() for item in wrapped)
        if heading in TABLES:
            headers, rows = TABLES[heading]
            lines.append(" | ".join(headers))
            for row in rows:
                lines.append(" | ".join(row))
    lines.extend(
        [
            "",
            "Appendix: Actual Android screen images were not available; capture Login, Sign-up,",
            "Dashboard, Customer list, Customer form, and Customer details from a configured emulator.",
        ]
    )
    return lines


def build_pdf() -> None:
    lines = pdf_lines()
    page_capacity = 54
    pages = [lines[index:index + page_capacity] for index in range(0, len(lines), page_capacity)]
    objects: list[bytes] = []
    objects.append(b"<< /Type /Catalog /Pages 2 0 R >>")
    page_ids = [5 + 2 * index for index in range(len(pages))]
    kids = " ".join(f"{page_id} 0 R" for page_id in page_ids)
    objects.append(f"<< /Type /Pages /Kids [{kids}] /Count {len(pages)} >>".encode("ascii"))
    objects.append(b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>")
    objects.append(b"<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>")

    for page_number, page_lines in enumerate(pages, start=1):
        page_id = 5 + 2 * (page_number - 1)
        content_id = page_id + 1
        commands = ["BT", "/F1 9 Tf", "50 792 Td", "12 TL"]
        for line in page_lines:
            text = ascii_text(line)
            text = text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")
            if line.startswith(("                         +", "                    +", "Staff ----")):
                commands.append("/F2 7 Tf")
            else:
                commands.append("/F1 9 Tf")
            commands.append(f"({text}) Tj T*")
        commands.extend(
            [
                "ET",
                "BT /F1 8 Tf 50 28 Td",
                f"(PrintShop Customer Manager  |  Page {page_number}) Tj",
                "ET",
            ]
        )
        stream = "\n".join(commands).encode("latin-1", "replace")
        page_object = (
            f"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
            f"/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> "
            f"/Contents {content_id} 0 R >>"
        ).encode("ascii")
        content_object = (
            f"<< /Length {len(stream)} >>\nstream\n".encode("ascii")
            + stream
            + b"\nendstream"
        )
        objects.extend([page_object, content_object])

    data = bytearray(b"%PDF-1.4\n% PrintShop Customer Manager Report\n")
    offsets = [0]
    for object_number, body in enumerate(objects, start=1):
        offsets.append(len(data))
        data.extend(f"{object_number} 0 obj\n".encode("ascii"))
        data.extend(body)
        data.extend(b"\nendobj\n")
    xref_offset = len(data)
    data.extend(f"xref\n0 {len(objects) + 1}\n".encode("ascii"))
    data.extend(b"0000000000 65535 f \n")
    for offset in offsets[1:]:
        data.extend(f"{offset:010d} 00000 n \n".encode("ascii"))
    data.extend(
        f"trailer\n<< /Size {len(objects) + 1} /Root 1 0 R >>\n"
        f"startxref\n{xref_offset}\n%%EOF\n".encode("ascii")
    )
    PDF_PATH.write_bytes(data)


if __name__ == "__main__":
    build_docx()
    build_pdf()
    print(f"Generated {DOCX_PATH}")
    print(f"Generated {PDF_PATH}")
