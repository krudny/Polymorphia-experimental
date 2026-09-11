"use client";

import Image from "next/image";
import NavigationArrow from "@/components/slider/NavigationArrow";
import DetailedSlideInfo from "@/components/slider/DetailedSlideInfo";
import clsx from "clsx";
import "./index.css";
import { useEffect, useRef } from "react";
import { animateSingleSlide } from "@/animations/Slider";
import { API_STATIC_URL } from "@/services/api";
import { SingleSlideProps } from "@/components/slider/types";

export default function SingleSlide({
  slide,
  position,
  prevSlideAction,
  nextSlideAction,
}: SingleSlideProps) {
  const sliderRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!sliderRef.current) {
      return;
    }
    animateSingleSlide(sliderRef.current, position);
  }, [position]);

  return (
    <div
      className={`carousel-item ${position === 0 ? "active" : ""}`}
      ref={sliderRef}
    >
      <div className="carousel-wrapper">
        <div className="carousel-image-wrapper">
          <div>
            <Image
              src={`${API_STATIC_URL}/${slide.imageUrl}`}
              fill
              alt={slide.name}
              sizes="(max-width: 1023px) 512px, (max-width: 1279px) 400px, (max-width: 1535px) 500px, 600px"
            />
          </div>
        </div>

        <div className={clsx("carousel-content-wrapper")}>
          <div className="carousel-content-inner-wrapper">
            <div className="carousel-content">
              <div>
                <NavigationArrow
                  direction="left"
                  onClick={prevSlideAction}
                  className="lg:hidden"
                />
                <NavigationArrow
                  direction="right"
                  onClick={nextSlideAction}
                  className="lg:hidden"
                />
                <h1>{slide.name}</h1>
                <h2>{slide.subtitle}</h2>
              </div>
              <p>{slide.description}</p>
            </div>

            {(slide.type === "ITEM" || slide.type === "CHEST") && (
              <div className="slide-details-wrapper">
                <DetailedSlideInfo
                  type={slide.type}
                  relatedRewards={slide.relatedRewards}
                />
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
