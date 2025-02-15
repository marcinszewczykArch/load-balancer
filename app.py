import sys
from bottle import Bottle, run

def create_app(port):
    app = Bottle()

    @app.get("/")
    def index():
        return f"Hello from port {port}!"

    @app.get("/test")
    def test():
        return f"Test from port {port}!"

    return app

if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8080
    app = create_app(port)
    run(app=app, host="localhost", port=port)
