package com.trello.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true) // doesnt crash when trello returns the fields i didnt map here
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Board {
    public String id;
    public String name;
    public String desc;
    public String shortUrl;
    public String closed;
}
