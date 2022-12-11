import json
import subprocess
import os

import qrcode
from threading import Thread

from PyQt5 import QtCore, QtGui, QtWidgets
from PyQt5.QtGui import QIcon
from PyQt5.QtWidgets import QProgressBar

from api import createSession, sendCitizenCardSigned

from PIL.ImageQt import ImageQt

PBAR_INTERVAL = 40
PBAR_VALUE = 5
PBAR_BACKWARDS = False
processing = False
WINDOW_WIDTH = 720
WINDOW_HEIGHT = 520

BAR_WIDTH = 200
BAR_HEIGHT = 25

proc1 = subprocess

StyleSheet = '''
#RedProgressBar {
	border: 2px solid #ee4460;
	border-radius: 5px;
	background-color: #E0E0E0;
}
#RedProgressBar::chunk {
	background-color: #ee4460;
	width: 10px; 
	margin: 0.5px;
}
#WhiteProgressBar {
	border: 2px solid #ee4460;
	border-radius: 5px;
	background: transparent;
}
#WhiteProgressBar::chunk {
	background-color: #E0E0E0;
	width: 10px; 
	margin: 0.5px;
}
'''


class ProgressBar(QProgressBar):
    def __init__(self, *args, **kwargs):
        super(ProgressBar, self).__init__(*args, **kwargs)


class Ui_MainWindow(object):
    CC_ID = None
    SESSION_ID = None
    PROVER_ID = None
    SESSION_TOKEN = None

    def setupUi(self, MainWindow):
        global red_bar_main, white_bar_main, red_bar_ble, white_bar_ble, proc1, scan_qr
        MainWindow.setObjectName("MainWindow")
        MainWindow.resize(WINDOW_WIDTH, WINDOW_HEIGHT)
        self.centralwidget = QtWidgets.QWidget(MainWindow)
        self.centralwidget.setObjectName("centralwidget")
        ######################
        self.frame = QtWidgets.QFrame(self.centralwidget)
        self.frame.setGeometry(QtCore.QRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT))
        self.frame.setFrameShape(QtWidgets.QFrame.StyledPanel)
        self.frame.setFrameShadow(QtWidgets.QFrame.Raised)
        self.frame.setObjectName("frame")
        ######################
        self.stackedWidget = QtWidgets.QStackedWidget(self.frame)
        self.stackedWidget.setGeometry(QtCore.QRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT))
        self.stackedWidget.setObjectName("stackedWidget")
        ######################

        #########################################
        ########### NEW WIDGET PAGE #############
        #########################################

        self.start_page = QtWidgets.QWidget()
        self.start_page.setObjectName("start_page")

        self.btn_start = QtWidgets.QPushButton(self.start_page)
        self.btn_start.setGeometry(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT)
        font = QtGui.QFont()
        font.setFamily("Arial")
        font.setPointSize(32)
        self.btn_start.setFont(font)
        self.btn_start.setObjectName("btn_start")
        self.btn_start.setStyleSheet("QPushButton"
                                     "{"
                                     "background-color : white;"
                                     "}"
                                     "QPushButton::pressed"
                                     "{"
                                     "background-color : silver;"
                                     "}"
                                     )
        self.btn_start.clicked.connect(self.start_experience)
        self.stackedWidget.addWidget(self.start_page)

        #########################################
        ########### NEW WIDGET PAGE #############
        #########################################

        self.final_page = QtWidgets.QWidget()
        self.final_page.setObjectName("final_page")

        self.final_page_text = QtWidgets.QLabel(self.final_page)
        self.final_page_text.setEnabled(True)
        self.final_page_text.setAlignment(QtCore.Qt.AlignCenter)
        self.final_page_text.setGeometry(QtCore.QRect(110, 20, 500, 140))
        font = QtGui.QFont()
        font.setFamily("Arial")
        font.setPointSize(22)
        font.setBold(False)
        font.setWeight(50)
        font.setStyleStrategy(QtGui.QFont.PreferDefault)
        self.final_page_text.setFont(font)
        self.final_page_text.setTextFormat(QtCore.Qt.AutoText)
        self.final_page_text.setAlignment(QtCore.Qt.AlignCenter)
        self.final_page_text.setObjectName("final_page_text")

        self.final_page_home = QtWidgets.QPushButton(self.final_page)
        self.final_page_home.setGeometry(QtCore.QRect(285, 200, 150, 130))
        font = QtGui.QFont()
        font.setFamily("Arial")
        font.setPointSize(12)
        self.final_page_home.setFont(font)
        self.final_page_home.setObjectName("btn_home")
        self.final_page_home.setIcon(QIcon('images/home.png'))
        self.final_page_home.setIconSize(QtCore.QSize(120, 135))
        self.final_page_home.clicked.connect(lambda: self.stackedWidget.setCurrentWidget(self.start_page))

        self.stackedWidget.addWidget(self.final_page)

        #########################################
        ########### NEW WIDGET PAGE #############
        #########################################

        self.cc_page = QtWidgets.QWidget()
        self.cc_page.setObjectName("cc_page")

        self.cc_sureThing_img = QtWidgets.QLabel(self.cc_page)
        self.create_sureThing_img(self.cc_sureThing_img)

        self.cc_instructions = QtWidgets.QLabel(self.cc_page)
        self.cc_instructions.setEnabled(True)
        self.cc_instructions.setGeometry(QtCore.QRect(120, 50, 600, 60))
        font = QtGui.QFont()
        font.setFamily("Arial")
        font.setPointSize(20)
        font.setBold(False)
        font.setWeight(50)
        font.setStyleStrategy(QtGui.QFont.PreferDefault)
        self.cc_instructions.setFont(font)
        self.cc_instructions.setTextFormat(QtCore.Qt.AutoText)
        self.cc_instructions.setAlignment(QtCore.Qt.AlignCenter)
        self.cc_instructions.setObjectName("cc_instructions")

        self.cc_reader = QtWidgets.QLabel(self.cc_page)
        self.cc_reader.setGeometry(QtCore.QRect(312, 120, 96, 96))
        self.cc_reader.setText("")
        self.cc_reader.setPixmap(QtGui.QPixmap("images/card_reader.png").scaledToWidth(96).scaledToHeight(96))
        self.cc_reader.setAlignment(QtCore.Qt.AlignCenter)

        self.cc_arrow = QtWidgets.QLabel(self.cc_page)
        self.cc_arrow.setGeometry(QtCore.QRect(337, 220, 46, 46))
        self.cc_arrow.setText("")
        self.cc_arrow.setPixmap(QtGui.QPixmap("images/up-arrow.png").scaledToWidth(46).scaledToHeight(46))
        self.cc_arrow.setAlignment(QtCore.Qt.AlignCenter)

        self.cc_card = QtWidgets.QLabel(self.cc_page)
        self.cc_card.setGeometry(QtCore.QRect(325, 275, 70, 104))
        self.cc_card.setText("")
        self.cc_card.setPixmap(QtGui.QPixmap("images/cc_resized.jpg").scaledToWidth(70).scaledToHeight(104))
        self.cc_card.setAlignment(QtCore.Qt.AlignCenter)

        self.red_pbar_cc = ProgressBar(self.cc_page, minimum=0, maximum=100, textVisible=False,
                                       objectName="RedProgressBar")
        self.red_pbar_cc.setGeometry(390, 160, 180, 25)
        self.red_pbar_cc.setVisible(False)

        self.white_pbar_cc = ProgressBar(self.cc_page, minimum=0, maximum=100, textVisible=False,
                                         objectName="WhiteProgressBar")
        self.white_pbar_cc.setGeometry(390, 160, 180, 25)
        self.white_pbar_cc.setVisible(False)

        self.red_pbar_cc.setValue(PBAR_INTERVAL)
        self.white_pbar_cc.setValue(0)

        self.btn_back_cc = QtWidgets.QPushButton(self.cc_page)
        self.create_back_button(self.btn_back_cc)
        self.btn_back_cc.clicked.connect(lambda: terminate(proc1, self.stackedWidget, self.start_page))

        self.cc_warning = QtWidgets.QLabel(self.cc_page)
        self.cc_warning.setEnabled(True)
        self.cc_warning.setGeometry(QtCore.QRect(450, 320, 350, 60))
        font = QtGui.QFont()
        font.setFamily("Arial")
        font.setPointSize(14)
        font.setBold(False)
        font.setWeight(50)
        font.setStyleStrategy(QtGui.QFont.PreferDefault)
        self.cc_warning.setFont(font)
        self.cc_warning.setTextFormat(QtCore.Qt.AutoText)
        self.cc_warning.setAlignment(QtCore.Qt.AlignCenter)
        self.cc_warning.setObjectName("cc_warning")
        self.cc_warning.setStyleSheet("color: red")
        self.cc_warning.setVisible(False)

        self.stackedWidget.addWidget(self.cc_page)

        #########################################
        ########### NEW WIDGET PAGE #############
        #########################################

        self.cc_pin_page = QtWidgets.QWidget()
        self.cc_pin_page.setObjectName("cc_pin_page")

        self.btn_back_sign_type = QtWidgets.QPushButton(self.cc_pin_page)
        self.create_back_button(self.btn_back_sign_type)
        self.btn_back_sign_type.clicked.connect(lambda: self.stackedWidget.setCurrentWidget(self.cc_sign_type_page))

        self.cc_pin_instructions = QtWidgets.QLabel(self.cc_pin_page)
        self.cc_pin_instructions.setEnabled(True)
        self.cc_pin_instructions.setGeometry(QtCore.QRect(120, 50, 600, 60))
        font = QtGui.QFont()
        font.setFamily("Arial")
        font.setPointSize(26)
        font.setBold(True)
        font.setWeight(50)
        font.setStyleStrategy(QtGui.QFont.PreferDefault)
        self.cc_pin_instructions.setFont(font)
        self.cc_pin_instructions.setTextFormat(QtCore.Qt.AutoText)
        self.cc_pin_instructions.setAlignment(QtCore.Qt.AlignCenter)
        self.cc_pin_instructions.setObjectName("cc_pin_instructions")

        self.cc_pin_description = QtWidgets.QLabel(self.cc_pin_page)
        self.cc_pin_description.setEnabled(True)
        self.cc_pin_description.setGeometry(QtCore.QRect(40, 150, 550, 60))
        font = QtGui.QFont()
        font.setFamily("Arial")
        font.setPointSize(19)
        font.setBold(False)
        font.setWeight(50)
        font.setStyleStrategy(QtGui.QFont.PreferDefault)
        self.cc_pin_description.setFont(font)
        self.cc_pin_description.setTextFormat(QtCore.Qt.AutoText)
        self.cc_pin_description.setAlignment(QtCore.Qt.AlignCenter)
        self.cc_pin_description.setObjectName("cc_pin_description")

        self.stackedWidget.addWidget(self.cc_pin_page)


        #########################################
        ########### NEW WIDGET PAGE #############
        #########################################


        self.scan_page = QtWidgets.QWidget()
        self.scan_page.setObjectName("scan_page")

        self.btn_back_scan = QtWidgets.QPushButton(self.scan_page)
        self.create_back_button_scan(self.btn_back_scan)
        self.btn_back_scan.clicked.connect(lambda: self.stackedWidget.setCurrentWidget(self.start_page))

        self.btn_check = QtWidgets.QPushButton(self.scan_page)
        self.btn_check.setGeometry(QtCore.QRect(605, 40, 90, 80))
        font = QtGui.QFont()
        font.setFamily("Arial")
        font.setPointSize(12)
        self.btn_check.setFont(font)
        self.btn_check.setObjectName("btn_home")
        self.btn_check.setIcon(QIcon('images/check.png'))
        self.btn_check.setIconSize(QtCore.QSize(75, 85))
        self.btn_check.clicked.connect(self.change_to_end_screen)

        self.check_arrow = QtWidgets.QLabel(self.scan_page)
        self.check_arrow.setGeometry(QtCore.QRect(632, 135, 32, 32))
        self.check_arrow.setText("")
        self.check_arrow.setPixmap(QtGui.QPixmap("images/up-arrow.png").scaledToWidth(32).scaledToHeight(32))
        self.check_arrow.setAlignment(QtCore.Qt.AlignCenter)

        self.check_text = QtWidgets.QLabel(self.scan_page)
        self.check_text.setEnabled(True)
        self.check_text.setGeometry(QtCore.QRect(585, 165, 120, 60))
        font = QtGui.QFont()
        font.setFamily("Arial")
        font.setPointSize(12)
        font.setBold(False)
        font.setWeight(50)
        font.setStyleStrategy(QtGui.QFont.PreferDefault)
        self.check_text.setFont(font)
        self.check_text.setTextFormat(QtCore.Qt.AutoText)
        self.check_text.setAlignment(QtCore.Qt.AlignCenter)
        self.check_text.setObjectName("check_text")

        self.scan_instruction = QtWidgets.QLabel(self.scan_page)
        self.scan_instruction.setEnabled(True)
        self.scan_instruction.setGeometry(QtCore.QRect(150, 0, 420, 60))
        font = QtGui.QFont()
        font.setFamily("Arial")
        font.setPointSize(17)
        font.setBold(True)
        font.setWeight(50)
        font.setStyleStrategy(QtGui.QFont.PreferDefault)
        self.scan_instruction.setFont(font)
        self.scan_instruction.setTextFormat(QtCore.Qt.AutoText)
        self.scan_instruction.setAlignment(QtCore.Qt.AlignCenter)
        self.scan_instruction.setObjectName("scan_instruction")

        scan_qr = QtWidgets.QLabel(self.scan_page)
        scan_qr.setGeometry(QtCore.QRect(175, 55, 360, 360))
        scan_qr.setAlignment(QtCore.Qt.AlignCenter)

        self.scan_visual_instruction = QtWidgets.QLabel(self.scan_page)
        self.scan_visual_instruction.setGeometry(QtCore.QRect(50, 170, 100, 170))
        self.scan_visual_instruction.setText("")
        self.scan_visual_instruction.setPixmap(
            QtGui.QPixmap("images/scan_instruction.png").scaledToWidth(890).scaledToHeight(669))
        self.scan_visual_instruction.setAlignment(QtCore.Qt.AlignCenter)

        self.scan_how = QtWidgets.QLabel(self.scan_page)
        self.scan_how.setEnabled(True)
        self.scan_how.setGeometry(QtCore.QRect(25, 180, 180, 60))
        font = QtGui.QFont()
        font.setFamily("Arial")
        font.setPointSize(13)
        font.setBold(True)
        font.setWeight(70)
        font.setStyleStrategy(QtGui.QFont.PreferDefault)
        self.scan_how.setFont(font)
        self.scan_how.setTextFormat(QtCore.Qt.AutoText)
        self.scan_how.setAlignment(QtCore.Qt.AlignLeft)
        self.scan_how.setObjectName("scan_how")

        self.stackedWidget.addWidget(self.scan_page)


        #########################################
        MainWindow.setCentralWidget(self.centralwidget)
        self.retranslateUi(MainWindow)
        self.stackedWidget.setCurrentIndex(0)
        QtCore.QMetaObject.connectSlotsByName(MainWindow)

    ############## Functions ################

    def start_experience(self):
        self.stackedWidget.setCurrentWidget(self.cc_page)
        self.CC_PIN_process()

    def create_back_button(self, object):
        object.setGeometry(QtCore.QRect(40, 40, 90, 80))
        font = QtGui.QFont()
        font.setFamily("Arial")
        font.setPointSize(12)
        object.setFont(font)
        object.setObjectName("btn_back")
        object.setIcon(QIcon('images/back.png'))
        object.setIconSize(QtCore.QSize(75, 85))

    def create_back_button_scan(self, object):
        object.setGeometry(QtCore.QRect(40, 40, 90, 80))
        font = QtGui.QFont()
        font.setFamily("Arial")
        font.setPointSize(12)
        object.setFont(font)
        object.setObjectName("btn_back")
        object.setIcon(QIcon('images/back.png'))
        object.setIconSize(QtCore.QSize(75, 85))
        object.clicked.connect(lambda: self.stackedWidget.setCurrentWidget(self.start_page))

    def create_sureThing_img(self, object):
        object.setGeometry(QtCore.QRect(600, 320, 36, 61))
        object.setText("")
        object.setPixmap(QtGui.QPixmap("images/surething.png"))
        object.setAlignment(QtCore.Qt.AlignCenter)

    def CC_PIN_process(self):
        print("CC PIN process")

        session = createSession()
        self.SESSION_ID = session["data"]["sessionId"]
        self.PROVER_ID = session["data"]["proverId"]
        self.SESSION_TOKEN = session["token"]
        print("Session Id: " + self.SESSION_ID)
        print("Prover Id: " + self.PROVER_ID)
        print("Session Token: " + self.SESSION_TOKEN)

        cc_thread = AsyncCCReaderWithPIN(self.red_pbar_cc, self.white_pbar_cc, self.cc_warning,
                                         self.stackedWidget, self.endorse, self.change_to_main_screen,
                                         self.SESSION_ID)
        cc_thread.start()

    def endorse(self, cc_id, citizenCardSignature, publicKey):
        global scan_qr

        print("Qualified endorse")

        self.CC_ID = cc_id
        qualifiedQrCode = {
            "citizenCardSignature": citizenCardSignature,
            "ccID": self.CC_ID,
            "sessionId": self.SESSION_ID,
            "proverId": self.PROVER_ID,
            "token": self.SESSION_TOKEN
        }
        jsonToBeSent = json.dumps(qualifiedQrCode)
        print("QRCode: " + jsonToBeSent)

        qr = qrcode.QRCode(
            version=None,
            error_correction=qrcode.constants.ERROR_CORRECT_L,
            box_size=10,
            border=2,
        )
        qr.add_data(jsonToBeSent)
        qr.make(fit=True)
        img = qr.make_image(fill_color="black", back_color="white")
        img.save("images/qualifiedQrCode.png")
        qim = ImageQt(img)
        scan_qr.setPixmap(QtGui.QPixmap.fromImage(qim).scaledToWidth(340).scaledToHeight(340))
        sendCitizenCardSigned(self.CC_ID, self.SESSION_TOKEN, open("signatureCC.txt", "rb").read(), publicKey)
        self.change_to_scan_screen()

    def QR_process(self):
        print("QR process")
        # startProgressBar(self.red_pbar_qr, self.white_pbar_qr) showing progress bar would give the wrong idea of processing
        qr_thread = AsyncQRCodeReader(self.red_pbar_qr, self.white_pbar_qr, self.stackedWidget, self.scan_page)
        qr_thread.start()

    def change_to_cc_pin_page(self):
        self.stackedWidget.setCurrentWidget(self.cc_pin_page)
        self.CC_PIN_process()

    def change_to_cc_pin_screen(self):
        self.stackedWidget.setCurrentWidget(self.cc_pin_page)

    def change_to_scan_screen(self):
        self.stackedWidget.setCurrentWidget(self.scan_page)

    def change_to_end_screen(self):
        global MODE
        self.final_page_text.setText("You are now authenticated.\n\nYou can remove your citizen card.")
        self.CC_ID = None
        self.SESSION_ID = None
        self.PROVER_ID = None
        self.SESSION_TOKEN = None
        self.stackedWidget.setCurrentWidget(self.final_page)

    def change_to_main_screen(self):
        self.stackedWidget.setCurrentWidget(self.start_page)

    def retranslateUi(self, MainWindow):
        _translate = QtCore.QCoreApplication.translate
        self.btn_start.setText(_translate("MainWindow", "Touch to start"))
        MainWindow.setWindowTitle(_translate("MainWindow", "SurePresence"))
        self.cc_instructions.setText(_translate("MainWindow", "Introduce your Citizen Card as showed below"))

        self.check_text.setText(_translate("MainWindow", "Press this after\nscanning"))
        self.scan_instruction.setText(_translate("MainWindow", "Scan this using your Inspector App"))
        self.scan_how.setText(_translate("MainWindow", "In the bottom bar\nof your app go to"))
        self.check_text.setText(_translate("MainWindow", "Press this after\nscanning"))

##############################################

class AsyncCCReaderWithPIN(Thread):
    """Thread that will read information from the citizen card with PIN"""

    def __init__(self, rbar, wbar, lbl, stacked, func, errorFunc, sessionId):
        super().__init__()
        self.rbar = rbar
        self.wbar = wbar
        self.lbl = lbl
        self.stacked = stacked
        self.func = func
        self.errorFunc = errorFunc
        self.sessionId = sessionId

    def run(self):
        try:
            global CC_ID

            info = readCardWithPIN(self.sessionId).read().decode("latin-1")  # in bytes
            info = info.rstrip("\r").rstrip("\n")
            cc_id = info.split(" ")[0] + " " + info.split(" ")[1] + info.split(" ")[2]
            signature = info.split(" ")[3]
            publicKey = info.split(" ")[4]

            print("CCid: " + cc_id)

            if (signature[:4] != "null"):
                global MODE
                MODE = 1
                self.lbl.setVisible(False)
                self.func(cc_id, signature, publicKey)
            else:
                self.errorFunc()

        except IndexError as e:
            print(e)
            self.errorFunc()

        except Exception as e:
            print(e)
            self.errorFunc()


######################################
########## External methods ##########
######################################

def readCardWithPIN(sessionId):
    global proc1
    print(sys.platform)
    if sys.platform == "linux":
        subprocess.run('javac -cp .:./lib/pteidlibj.jar mainPIN.java', shell=True, stdout=subprocess.PIPE,
                              stderr=subprocess.STDOUT)
        proc1 = subprocess.Popen(
            args=["java", "-Djava.library.path=/usr/local/lib", "-cp", "lib/pteidlibj.jar:.", "mainPIN", sessionId],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, preexec_fn=os.setsid)

    elif sys.platform == "win32":
        subprocess.run('javac -cp lib\pteidlibj.jar mainPIN.java', stdout=subprocess.PIPE,
                              stderr=subprocess.STDOUT)
        # proc1 = subprocess.run('java -cp lib/pteidlibj.jar ;. mainPIN ' + pin, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        proc1 = subprocess.run('java -cp lib/pteidlibj.jar ;. mainPIN', stdout=subprocess.PIPE,
                               stderr=subprocess.STDOUT)

    return proc1.stdout

def terminate(proc, widget, page):
    try:
        os.killpg(os.getpgid(proc.pid), signal.SIGTERM)
        print("DEAD")
    except AttributeError:
        pass
    widget.setCurrentWidget(page)


if __name__ == "__main__":
    import sys

    app = QtWidgets.QApplication(sys.argv)
    app.setStyleSheet(StyleSheet)
    MainWindow = QtWidgets.QMainWindow()
    ui = Ui_MainWindow()
    ui.setupUi(MainWindow)
    MainWindow.setWindowIcon(QIcon("images/surething.png"))
    MainWindow.show()
    sys.exit(app.exec_())
