use actix_web::{App, HttpServer};

mod data_processing;
use data_processing::process_temp;

use log::LevelFilter;
use simplelog::{Config, SimpleLogger};

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    let _ = SimpleLogger::init(LevelFilter::Info, Config::default());
    
    HttpServer::new(|| App::new().service(process_temp))
        .bind(("0.0.0.0", 9000))?
        .run()
        .await
}
